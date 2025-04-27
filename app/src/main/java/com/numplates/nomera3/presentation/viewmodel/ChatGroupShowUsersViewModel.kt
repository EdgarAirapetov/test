package com.numplates.nomera3.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.google.gson.Gson
import com.meera.core.extensions.fromJson
import com.meera.core.extensions.toInt
import com.meera.core.network.websocket.WebSocketMainChannel
import com.meera.core.network.websocket.WebSocketResponseException
import com.meera.core.preferences.AppSettings
import com.meera.db.DataStore
import com.meera.db.models.chatmembers.ChatMember
import com.numplates.nomera3.data.newmessenger.USER_TYPE_ADMIN
import com.numplates.nomera3.data.newmessenger.USER_TYPE_CREATOR
import com.numplates.nomera3.data.newmessenger.USER_TYPE_MEMBER
import com.numplates.nomera3.data.newmessenger.response.ErrorMessage
import com.numplates.nomera3.data.newmessenger.response.GroupChatAdmins
import com.numplates.nomera3.data.newmessenger.response.ResponseWrapperWebSock
import com.numplates.nomera3.domain.interactornew.GetUserUidUseCase
import com.numplates.nomera3.modules.chat.domain.GroupChatRepository
import com.numplates.nomera3.modules.chat.domain.interactors.ChatInteractorImlp
import com.numplates.nomera3.modules.moments.show.domain.SubscribeMomentsEventsUseCase
import com.numplates.nomera3.modules.moments.show.domain.UserMomentsStateUpdateModel
import com.numplates.nomera3.modules.moments.show.domain.model.MomentRepositoryEvent
import com.numplates.nomera3.modules.user.domain.usecase.BlockStatusUseCase
import com.numplates.nomera3.modules.user.domain.usecase.DefBlockParams
import com.numplates.nomera3.presentation.view.adapter.newchat.ChatGroupMembersBoundaryCallback
import com.numplates.nomera3.presentation.viewmodel.viewevents.ChatGroupViewEvent
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.Executors
import javax.inject.Inject

class ChatGroupShowUsersViewModel @Inject constructor(
    private val webSocketMainChannel: WebSocketMainChannel,
    private val gson: Gson,
    private val dataStore: DataStore,
    private val appSettings: AppSettings,
    private val blockUser: BlockStatusUseCase,
    private val getUserUidCase: GetUserUidUseCase,
    private val chatInteractor: ChatInteractorImlp,
    private val groupChatRepository: GroupChatRepository,
    private val momentsObserverUseCase: SubscribeMomentsEventsUseCase
) : BaseViewModel() {

    private val disposables = CompositeDisposable()

    lateinit var livePagedUsers: LiveData<PagedList<ChatMember>>

    val liveViewEventShowUsers = MutableLiveData<ChatGroupViewEvent>()

    val liveChatUserRole = MutableLiveData<String>()

    private var observeGetMessagesDisposable: Disposable? = null

    override fun onCleared() {
        super.onCleared()
        disposables.dispose()
    }

    // ловим событие удаления пользователя из группового чата. если администратор удаляет пользователя
    // из группового чата, то остальным пользователям находящимся на экране со списком пользователей
    // группового чата придет событие с кодом 2.0 -> REMOVED_FROM_GROUP_CHAT, в этот момент обновляем
    // список
    fun startObserveRemoveMemberEvents() {
        observeGetMessagesDisposable?.dispose()
        observeGetMessagesDisposable = webSocketMainChannel.observeGetMessages()
            .subscribeOn(Schedulers.io())
            .map {
                val chatEventCode = it?.payload?.get("code")
                    ?.toString()
                    ?.toDoubleOrNull()
                    ?: -1.0

                val deletedUserId = (it?.payload?.get("metadata") as? Map<*, *>)
                    ?.toMap()
                    ?.get("user_id")
                    ?.toString()
                    ?.toDoubleOrNull()
                    ?.toLong()
                    ?: -1L

                val roomId = it?.payload?.get("room_id")
                    ?.toString()
                    ?.toDoubleOrNull()
                    ?.toLong()
                    ?: -1L

                Triple(chatEventCode, deletedUserId, roomId)
            }
            .observeOn(Schedulers.io())
            .subscribe {
                val code = it.first
                val userId = it.second
                val roomId = it.third

                // если код события == REMOVED_FROM_GROUP_CHAT
                // и значения у deletedUserId, roomId валидные
                // то переходим к удалению
                if (code == 2.0 && userId != -1L && roomId != -1L) {
                    // проверка чтобы не делать повторное удаление из БД на телефоне
                    // пользователя который сделал удаление
                    if (dataStore.daoChatMembers().getMemberById(userId, roomId).isNotEmpty()) {
                        dataStore.daoChatMembers().deleteMemberById(userId, roomId)
                    }
                }
            }

        observeGetMessagesDisposable?.let { disposables.add(it) }
    }

    fun getUserUid() = getUserUidCase.invoke()

    fun addAdminsToCache(users: List<ChatMember>) {
        groupChatRepository.clearCachedAdmins()
        val adminIds = users.filter { it.type ==  USER_TYPE_ADMIN}.map { it.userId }
        groupChatRepository.setAdminsToCache(adminIds)
    }

    // TODO: Добавить в БД запись из roomId
    fun initPagingMembers(roomId: Long?, isAdmin: Boolean) {
        val localRoomId = roomId ?: NOT_YET_CREATED_ROOM_ID
        val dataSource = if (isAdmin) {
            // Show admins
            dataStore.daoChatMembers().getAdmins(localRoomId, USER_TYPE_CREATOR, USER_TYPE_ADMIN)
        } else {
            // Show all members
            dataStore.daoChatMembers().getAllMembersDataSource(localRoomId)
        }

        val builder = LivePagedListBuilder(dataSource, pageListConfig)
            .setFetchExecutor(Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()))

        if (roomId != null) {
            val boundaryCallback = ChatGroupMembersBoundaryCallback(
                roomId = roomId,
                webSocketMainChannel = webSocketMainChannel,
                dataStore = dataStore,
                gson = gson,
            )
            builder.setBoundaryCallback(boundaryCallback)
        }

        livePagedUsers = builder.build()
    }

    /**
     * Check user is admin
     */
    fun checkGroupAdmin(roomId: Long?) {
        Timber.e("CHECK Group Admin")
        viewModelScope.launch(Dispatchers.IO) {
            var role = USER_TYPE_MEMBER
            // Local
            chatInteractor.getAllMembers(roomId ?: NOT_YET_CREATED_ROOM_ID).forEach { member ->
                if (member.user.userId == appSettings.readUID()) {
                    role = member.type
                }
            }
            // Remote
            val payload = mapOf(
                "room_id" to (roomId ?: 0),
                "limit" to 200,
                "offset" to 0)
            val responsePayload = webSocketMainChannel.pushGetAdmins(payload).payload
            val responseObj = gson.fromJson<ResponseWrapperWebSock<GroupChatAdmins>>(gson.toJson(responsePayload))
            responseObj.response?.admins?.forEach { member ->
                if (member.user.userId == appSettings.readUID()) {
                    role = member.type
                }
            }
            liveChatUserRole.postValue(role)
        }
    }


    fun setUserAdmin(roomId: Long?, userId: Long) {
        roomId?.let { id ->
            val payload = hashMapOf(
                "user_ids" to mutableListOf(userId),
                "room_id" to roomId
            )
            disposables.add(
                webSocketMainChannel.pushAddAdmins(payload)
                    .subscribeOn(Schedulers.io())
                    .subscribe({ response ->
                        Timber.d("Successfully ADD Admin: ${gson.toJson(response.payload)}")
                        // Delete from Db
                        dataStore.daoChatMembers().deleteMemberById(userId, roomId)
                    }, { handleError(it) })
            )
        }
    }


    fun removeUserFromChat(roomId: Long?, userId: Long) {
        Timber.d("Remove USER: room: $roomId uID: $userId")
        roomId?.let {
            val payload = hashMapOf(
                "room_id" to roomId,
                "user_id" to userId
            )
            disposables.add(
                webSocketMainChannel.pushRemoveUser(payload)
                    .subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.io())
                    .subscribe({ response ->
                        Timber.d("Successfully REMOVE User: ")
                        // Delete from Db
                        dataStore.daoChatMembers().deleteMemberById(userId, roomId)
                    }, { handleError(it) })
            )
        }
    }


    fun removeAdminFromChat(roomId: Long?, userId: Long) {
        Timber.d("Remove ADMIN from chat - room: $roomId uID: $userId")
        roomId?.let {
            val payload = hashMapOf(
                "room_id" to roomId,
                "user_id" to userId
            )
            disposables.add(
                webSocketMainChannel.pushRemoveAdmin(payload)
                    .subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.io())
                    .subscribe({ response ->
                        Timber.d("Successfully REMOVE ADMIN: ")
                        // Delete from Db
                        dataStore.daoChatMembers().deleteMemberById(userId, roomId)
                    }, { handleError(it) })
            )
        }
    }


    /**
     * Block user
     */
    fun blockChatUser(userId: Long, remoteUserId: Long, isBlock: Boolean, user: ChatMember? = null) {
        val params = DefBlockParams(
            userId = userId,
            remoteUserId = remoteUserId,
            isBlocked = isBlock
        )
        viewModelScope.launch(Dispatchers.IO) {
            {
                launch (Dispatchers.Main) {
                    val event = ChatGroupViewEvent.OnSuccessBlockUser()
                    event.isBlock = isBlock
                    liveViewEventShowUsers.postValue(event)
                    user?.user?.isBlockedByMe = isBlock
                }
            }
            {
                liveViewEventShowUsers.postValue(ChatGroupViewEvent.OnFailureBlockUser)
            }
            blockUser.invoke(
                params = params
            )
        }
    }

    fun observeMoments() {
        momentsObserverUseCase.invoke()
            .onEach { momentEvent ->
                if (momentEvent is MomentRepositoryEvent.UserMomentsStateUpdated) {
                    val state = momentEvent.userMomentsStateUpdate
                    updateItemWithMoments(state)
                }
            }
            .launchIn(viewModelScope)
    }

    private fun updateItemWithMoments(state: UserMomentsStateUpdateModel) {
        viewModelScope.launch(Dispatchers.IO) {
            val member = dataStore.daoChatMembers().getMember(state.userId)
            val updated = member.copy(user = member.user.copy(moments = member.user.moments?.copy(
                hasMoments = state.hasMoments.toInt(),
                hasNewMoments = state.hasNewMoments.toInt()
            )))
            dataStore.daoChatMembers().update(updated)
        }
    }

    private fun handleError(error: Throwable) {
        val err = error as WebSocketResponseException
        val msg = gson.fromJson<ResponseWrapperWebSock<ErrorMessage>>(gson.toJson(err.getPayload()))

        val event = ChatGroupViewEvent.ErrorUserMessage()
        event.mesage = msg.response?.error

        liveViewEventShowUsers.postValue(event)
    }

    private val pageListConfig = PagedList.Config.Builder()
        .setPageSize(20)
        //.setPrefetchDistance(NETWORK_PAGE_SIZE * 3)
        .setEnablePlaceholders(false)
        .build()
}
