package com.numplates.nomera3.modules.chat.helpers.chatinit

import com.meera.core.extensions.toBoolean
import com.meera.db.models.dialog.DialogEntity
import com.meera.db.models.dialog.DialogStyle
import com.numplates.nomera3.data.newmessenger.ROOM_TYPE_DIALOG
import com.numplates.nomera3.modules.baseCore.helper.HolidayInfoHelper
import com.numplates.nomera3.modules.chat.domain.interactors.MessagesInteractor
import com.numplates.nomera3.modules.chat.domain.usecases.CacheCompanionUserForChatInitUseCase
import com.numplates.nomera3.modules.chat.drafts.domain.GetAllDraftsUseCase
import com.numplates.nomera3.modules.chatrooms.domain.interactors.RoomsInteractor
import com.numplates.nomera3.modules.chatrooms.pojo.RoomTimeType
import com.numplates.nomera3.presentation.utils.networkconn.NetworkStatusProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import kotlin.math.max

class ChatInitializer @Inject constructor(
    private val networkStatusProvoder: NetworkStatusProvider,
    private val roomsInteractor: RoomsInteractor,
    private val messagesInteractor: MessagesInteractor,
    private val chatInitProfileMapper: ChatInitProfileMapper,
    private val getAllDraftsUseCase: GetAllDraftsUseCase,
    private val cacheCompanionUserUseCase: CacheCompanionUserForChatInitUseCase,
    private val holidayInfoHelper: HolidayInfoHelper,
) {

    private var scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    val chatInitStateFlow: Flow<ChatInitState?>
        get() = _chatInitStateFlow

    private val _chatInitStateFlow = MutableStateFlow<ChatInitState?>(null)

    fun init(
        data: ChatInitData,
        scope: CoroutineScope
    ) {
        this.scope = scope
        scope.launch { resolveChatInitType(data) }
    }

    private suspend fun resolveChatInitType(data: ChatInitData) {
        when(data.initType){
            ChatInitType.FROM_LIST_ROOMS -> initChatFromRooms(data)
            ChatInitType.FROM_PROFILE -> initChatFromProfile(data)
        }
    }

    private suspend fun initChatFromRooms(data: ChatInitData) {
        val roomId = data.roomId ?: 0L
        loadRoomData(roomId)
    }

    private suspend fun initChatFromProfile(data: ChatInitData) {
        val userId = data.userId ?: 0L
        checkRoomExistsOnServer(
            userId = userId,
            onResult = { isRoomExists, roomId ->
                if (isRoomExists) {
                    loadRoomData(
                        roomId = roomId ?: 0L,
                        onRoomNotExists = { waitCreationFirstMessageWhenChatNotExists(userId) }
                    )
                } else {
                    waitCreationFirstMessageWhenChatNotExists(userId)
                }
            },
            onFailNetwork = { errMessage ->
                Timber.e(errMessage)
                loadRoomFromDbIfExists(companionUid = userId)
            }
        )
    }

    private suspend fun waitCreationFirstMessageWhenChatNotExists(userId: Long) {
        val cachedProfileInitData = cacheCompanionUserUseCase.invoke(null)
        val roomStyle = getChatRoomStyle()
        val updatedProfileInitData = cachedProfileInitData?.copy(
            style = ChatInitRoomStyleData(
                styleBackground = roomStyle.background,
                styleType = roomStyle.type
            )
        )
        val emptyRoom = chatInitProfileMapper.mapToEmptyRoom(updatedProfileInitData)
        _chatInitStateFlow.emit(ChatInitState.OnTransitedByProfile(emptyRoom))
        observeCreateNewRoom(userId)
    }

    private suspend fun loadRoomFromDbIfExists(companionUid: Long) {
        val room = roomsInteractor.getRoomByCompanionFlow(companionUid).firstOrNull()
        if(room != null) {
            loadRoomData(room.roomId)
        } else {
            waitCreationFirstMessageWhenChatNotExists(companionUid)
        }
    }

    private suspend fun checkRoomExistsOnServer(
        userId: Long,
        onResult: suspend (isRoomExists: Boolean, roomId: Long?) -> Unit,
        onFailNetwork: suspend (errMessage: String?) -> Unit
    ) {
        runCatching {
            val (isRoomExists, roomId) = roomsInteractor.checkRoomExists(
                userId = userId,
                roomType = ROOM_TYPE_DIALOG
            )
            onResult(isRoomExists, roomId)
        }.onFailure {
            Timber.e(it)
            onFailNetwork.invoke(it.message)
        }
    }

    private fun observeCreateNewRoom(userId: Long) {
        roomsInteractor.getRoomByCompanionFlow(userId)
            .distinctUntilChanged { old, new ->
                old?.roomId == new?.roomId
            }
            .onEach { room ->
                room?.let {
                    observeCreateFirstMessage(room.roomId)
                }
            }
            .flowOn(Dispatchers.IO)
            .launchIn(scope)
    }

    private fun observeCreateFirstMessage(roomId: Long) {
        messagesInteractor.observeCountMessages(roomId)
            .distinctUntilChanged()
            .filter { it <= 1L }
            .onEach { initializeNewRoomCreated(roomId) }
            .launchIn(scope)
    }

    private suspend fun initializeNewRoomCreated(roomId: Long) {
        val room = roomsInteractor.getRoom(roomId)
        _chatInitStateFlow.emit(ChatInitState.OnTransitedByListRooms(room))
    }

    private suspend fun loadRoomData(
        roomId: Long,
        onRoomNotExists: suspend () -> Unit = {}
    ) {
        val room = getRoomFromDb(roomId)
        if (room == null) {
            onRoomNotExists.invoke()
        } else {
            _chatInitStateFlow.emit(ChatInitState.OnTransitedByListRooms(room))
            loadRoomDataFromNetworkAndSyncDb(roomId, room)
        }
    }

    private suspend fun loadRoomDataFromNetworkAndSyncDb(roomId: Long, dbRoom: DialogEntity?) {
        runCatching {
            if (!isInternetAvailable()) return@runCatching
            val rooms = roomsInteractor.getRooms(type = RoomTimeType.CURRENT_UPDATE)
            val roomsWithDraft = calculateRoomsTimestampWithDraft(rooms)
            val networkRoom = roomsWithDraft.firstOrNull { room -> room.roomId == roomId }
            if (networkRoom != dbRoom) {
                _chatInitStateFlow.emit(ChatInitState.OnUpdateRoomData(networkRoom))
                saveLastUpdatedRoomTsIntoDb()
                updateRoomsIntoDb(roomsWithDraft)
            }
        }.onFailure {
            Timber.e("ERROR ChatInit get room and syncDb:$it")
        }
    }

    private suspend fun saveLastUpdatedRoomTsIntoDb() {
        roomsInteractor.insertAppSettings()
    }

    private suspend fun calculateRoomsTimestampWithDraft(rooms: List<DialogEntity>): List<DialogEntity> {
        val drafts = getAllDraftsUseCase.invoke()
        val roomsDataMapped = rooms.map { room ->
            val draft = drafts.firstOrNull { draft -> draft.roomId == room.roomId }
            val lastMsgCreatedAt = room.lastMessage?.createdAt ?: 0
            val lastMessageUpdatedTs = if (draft != null) {
                max(draft.lastUpdatedTimestamp, lastMsgCreatedAt)
            } else {
                lastMsgCreatedAt
            }
            room.copy(
                isHidden = room.companion.blacklistedByMe.toBoolean(),
                lastMessageUpdatedAt = lastMessageUpdatedTs
            )
        }
        return roomsDataMapped
    }

    private suspend fun getRoomFromDb(roomId: Long) = roomsInteractor.getRoom(roomId)

    private suspend fun updateRoomsIntoDb(rooms: List<DialogEntity>) {
        roomsInteractor.insertRooms(rooms)
    }

    private fun isInternetAvailable() = networkStatusProvoder.isInternetConnected()

    private fun getChatRoomStyle(): DialogStyle {
        val info = holidayInfoHelper.currentHoliday()
        return DialogStyle(
            info.chatRoomEntity.background_dialog ?: "",
            info.chatRoomEntity.type ?: ""
        )
    }
}

