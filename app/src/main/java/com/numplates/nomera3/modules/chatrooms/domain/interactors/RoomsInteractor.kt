package com.numplates.nomera3.modules.chatrooms.domain.interactors

import com.meera.db.models.dialog.DialogEntity
import com.meera.db.models.message.MessageEntity
import com.numplates.nomera3.data.newmessenger.CHAT_ITEM_TYPE_EVENT
import com.numplates.nomera3.domain.interactornew.GetRoomDataUseCase
import com.numplates.nomera3.modules.chat.drafts.domain.GetAllDraftsUseCase
import com.numplates.nomera3.modules.chatrooms.domain.usecase.CheckRoomExistsUseCase
import com.numplates.nomera3.modules.chatrooms.domain.usecase.DeleteRoomAndMessagesUseCase
import com.numplates.nomera3.modules.chatrooms.domain.usecase.GetRoomByCompanionFlowUseCase
import com.numplates.nomera3.modules.chatrooms.domain.usecase.GetRoomUseCase
import com.numplates.nomera3.modules.chatrooms.domain.usecase.GetRoomsByTimestampUseCase
import com.numplates.nomera3.modules.chatrooms.domain.usecase.GetRoomsUseCase
import com.numplates.nomera3.modules.chatrooms.domain.usecase.GetUnsentMessagesUseCase
import com.numplates.nomera3.modules.chatrooms.domain.usecase.IncrementUnreadMessageUseCase
import com.numplates.nomera3.modules.chatrooms.domain.usecase.InsertDialogsUseCase
import com.numplates.nomera3.modules.chatrooms.domain.usecase.InsertSettingsUseCase
import com.numplates.nomera3.modules.chatrooms.domain.usecase.ObserveReloadDialogsUseCase
import com.numplates.nomera3.modules.chatrooms.domain.usecase.SaveRoomsIntoDbUseCase
import com.numplates.nomera3.modules.chatrooms.domain.usecase.SendRoomsToServerUseCase
import com.numplates.nomera3.modules.chatrooms.pojo.RoomTimeType
import com.numplates.nomera3.presentation.model.enums.ChatEventEnum
import com.numplates.nomera3.presentation.utils.parseUniquename
import com.numplates.nomera3.presentation.viewmodel.RoomsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import kotlin.math.max

private const val DEFAULT_SERVER_LIMIT = 100

class RoomsInteractor @Inject constructor(
    private val getRoomsUseCase: GetRoomsUseCase,
    private val getRoomDataUseCase: GetRoomDataUseCase,
    private val getRoomUseCase: GetRoomUseCase,
    private val getRoomsByTimestampUseCase: GetRoomsByTimestampUseCase,
    private val checkRoomExistsUseCase: CheckRoomExistsUseCase,
    private val getRoomByCompanionFlowUseCase: GetRoomByCompanionFlowUseCase,
    private val getAllDraftsUseCase: GetAllDraftsUseCase,
    private val incrementUnreadMessageUseCase: IncrementUnreadMessageUseCase,
    private val getUnsentMessagesUseCase: GetUnsentMessagesUseCase,
    private val saveRoomsIntoDbUseCase: SaveRoomsIntoDbUseCase,
    private val sendRoomsToServerUseCase: SendRoomsToServerUseCase,
    private val insertSettingsUseCase: InsertSettingsUseCase,
    private val insertDialogsUseCase: InsertDialogsUseCase,
    private val deleteRoomAndMessagesUseCase: DeleteRoomAndMessagesUseCase,
    private val observeReloadDialogsUseCase: ObserveReloadDialogsUseCase
) {

    private var canRefresh = true
    private val unsentRooms = mutableSetOf<Long>()

    suspend fun deleteRoomAndMessages(roomId: Long, isBoth: Boolean) =
        deleteRoomAndMessagesUseCase.invoke(roomId, isBoth)

    /**
     * @param updatedAt - последнее (max) значение updatedAt в таблице диалогов
     * @param topTs -  самое раннее (min) значение updatedAt в таблице диалогов
     * @param limit - количество записей на странице
     */
    suspend fun getRooms(
        type: RoomTimeType,
        limit: Int = RoomsViewModel.ROOM_DIALOGS_PAGE_SIZE
    ): List<DialogEntity> {
        return getRoomsUseCase.invoke(
            type = type,
            limit = limit
        )
    }

    suspend fun getRoom(roomId: Long?) = getRoomUseCase.invoke(roomId)

    suspend fun insertRooms(rooms: List<DialogEntity>) = insertDialogsUseCase.invokeSuspend(rooms)

    suspend fun insertAppSettings() = withContext(Dispatchers.IO) {
        insertSettingsUseCase.invoke()
    }

    /**
     * @return Pair(isRoomExists, roomId)
     */
    suspend fun checkRoomExists(userId: Long, roomType: String): Pair<Boolean, Long> {
        return checkRoomExistsUseCase.invoke(userId, roomType)
    }

    /**
     * Get all rooms with pagination
     * isReloadRooms = true когда тебе событие приходит reload_dialogs. В остальных случаях false
     */
    suspend fun getRooms(delayMs: Long = 0) = withContext(Dispatchers.IO) {
        if (!canRefresh) return@withContext

        if (delayMs > 0) delay(delayMs)

        getRoomsWhereUndeliveredExists()

        runCatching {
            canRefresh = false
            var localTopTs = System.currentTimeMillis()
            do {
                val result = getRoomsByTimestampUseCase.invoke(
                    limit = DEFAULT_SERVER_LIMIT,
                    updatedAt = null,
                    topTs = localTopTs
                )
                handleGetRoomsResponse(result)
                localTopTs = result.minOf { it.updatedAt }
            } while (result.size == DEFAULT_SERVER_LIMIT)
            canRefresh = true
        }.onFailure { error ->
            Timber.e("ERROR Response REST GetRooms:$error")
            canRefresh = true
        }
    }

    suspend fun getRoom(): DialogEntity? {
        runCatching {
            getRoomsUseCase.invoke(
                type = RoomTimeType.ROOMS_MAX_UPDATE,
                limit = RoomsViewModel.ROOM_DIALOGS_PAGE_SIZE
            )
        }.onSuccess { dialogs ->
            // Save updatedAt time
            insertSettingsUseCase.invoke()
            // Save rooms in Db
            val drafts = getAllDraftsUseCase.invoke()
            val newDialogs = dialogs.map { d ->
                val draft = drafts.firstOrNull { draft -> draft.roomId == d.roomId }
                val lastMsgCreatedAt = d.lastMessage?.createdAt ?: 0
                val lastMessageUpdatedTs = if (draft != null) {
                    max(draft.lastUpdatedTimestamp, lastMsgCreatedAt)
                } else {
                    lastMsgCreatedAt
                }
                d.creator.userId?.let { uid ->
                    d.creatorUid = uid
                    d.companionUid = uid
                    d.lastMessageUpdatedAt = lastMessageUpdatedTs
                }
                return@map d
            }

            insertDialogsUseCase.invoke(newDialogs)
            return newDialogs.firstOrNull()
        }.onFailure {
            Timber.e("ERROR get Room data")
        }
        return null
    }

    suspend fun dirtyHackForUnreadEvent(isUpdatedRow: Int, message: MessageEntity) {
        if (isUpdatedRow == 0) {
            Timber.d("[DIRTY HACK] UPDATEe isUpdatedLastMessage")
            reloadRooms {
                /* Increase unread counter when  */
                if (message.type == CHAT_ITEM_TYPE_EVENT && (message.eventCode == ChatEventEnum.NEW_GROUP_ROOM.state
                        || message.eventCode == ChatEventEnum.NEW_GROUP_ROOM_ADDED.state
                        || message.eventCode == ChatEventEnum.REMOVED_FROM_GROUP_CHAT.state)
                ) {
                    incrementUnreadMessageUseCase.invoke(message.roomId)
                }
            }
        }
        // Если приходит сообщение с упоминанием то рефрешим комнату для появления значка @
        // Иконка меншона отображается только при сообщении содержащем упоминание пользователя А по уникальному имени
        else if (isUpdatedRow == 1 && !message.tags.isNullOrEmpty()) {
            reloadRooms { /** STUB */ }
        }
    }

    suspend fun reloadRooms(callback: () -> Unit) {
        Timber.e("RELOAD Rooms => RoomsInteractor")
        runCatching {
            getRoomsUseCase.invoke(
                type = RoomTimeType.ROOMS_MAX_UPDATE,
                limit = RoomsViewModel.ROOM_DIALOGS_PAGE_SIZE
            )
        }.onSuccess { rooms ->
            handleGetRoomsResponse(rooms)
            callback.invoke()
        }.onFailure { error ->
            Timber.e("ERROR Response REST GetRooms:$error")
        }
    }

    suspend fun getRoomData(roomId: Long): DialogEntity? = getRoomDataUseCase.invoke(roomId)

    fun getRoomByCompanionFlow(companionId: Long): Flow<DialogEntity?> =
        getRoomByCompanionFlowUseCase.invoke(companionId)

    fun observeReloadDialogs() = observeReloadDialogsUseCase.invoke()

    /**
     * Получаем список комнат, в котроых существуют непрочитанные сообщения
     */
    private suspend fun getRoomsWhereUndeliveredExists() {
        val messages = getUnsentMessagesUseCase.invoke()
        unsentRooms.clear()
        messages.forEach {
            unsentRooms.add(it.roomId)
        }
    }

    private suspend fun handleGetRoomsResponse(
        rooms: List<DialogEntity>,
        success: (List<DialogEntity>) -> Unit = {}
    ) {
        Timber.d("handleGetRoomsResponse called with rooms: $rooms")
        try {
            if (rooms.isNotEmpty()) {
                canRefresh = false
                handleIncomingRooms(rooms)
                val drafts = runCatching {
                    getAllDraftsUseCase.invoke()
                }.getOrElse {
                    Timber.d(it)
                    emptyList()
                }
                saveRoomsIntoDbUseCase.invoke(rooms, drafts, unsentRooms)
                success.invoke(rooms)
                canRefresh = true
            }
        } catch (e: Exception) {
            canRefresh = true
            Timber.e("ERROR handling GetRooms result:$e")
            e.printStackTrace()
        }
    }

    private suspend fun handleIncomingRooms(rooms: List<DialogEntity>?) {
        if (rooms == null) return
        val roomIds = mutableListOf<Long>()
        rooms.forEach { room ->
            roomIds.add(room.roomId)
            parseLastMessageUniqueName(room)
        }
        // Mark room as read
        if (roomIds.isEmpty()) return
        sendRoomsToServerUseCase.invoke(roomIds)
    }

    private fun parseLastMessageUniqueName(room: DialogEntity) {
        val msg = room.lastMessage
        msg?.tags?.let { tags ->
            msg.tagSpan = parseUniquename(msg.content, tags)
            msg.content = msg.tagSpan?.text ?: msg.content
        }
    }
}
