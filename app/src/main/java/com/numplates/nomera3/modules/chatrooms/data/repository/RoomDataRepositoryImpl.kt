package com.numplates.nomera3.modules.chatrooms.data.repository


import androidx.lifecycle.LiveData
import com.google.gson.Gson
import com.meera.core.di.scopes.AppScope
import com.meera.core.extensions.empty
import com.meera.core.extensions.toBoolean
import com.meera.core.network.websocket.WebSocketMainChannel
import com.meera.core.preferences.AppSettings
import com.meera.db.DataStore
import com.meera.db.models.AppSettingsEntity
import com.meera.db.models.dialog.DialogEntity
import com.meera.db.models.message.MessageEntity
import com.numplates.nomera3.data.network.ApiMain
import com.numplates.nomera3.data.newmessenger.ROOM_TYPE_GROUP
import com.numplates.nomera3.data.newmessenger.response.ResponseRoomId
import com.numplates.nomera3.data.newmessenger.response.ResponseWrapperWebSock
import com.numplates.nomera3.data.websocket.STATUS_ERROR
import com.numplates.nomera3.data.websocket.STATUS_OK
import com.numplates.nomera3.modules.chat.ChatPayloadKeys
import com.numplates.nomera3.modules.chat.drafts.domain.entity.DraftModel
import com.numplates.nomera3.modules.chatrooms.data.api.GetRoomsResponse
import com.numplates.nomera3.modules.chatrooms.data.api.RoomsApi
import com.numplates.nomera3.modules.chatrooms.data.entity.RoomsSettingsModel
import com.numplates.nomera3.modules.chatrooms.pojo.RoomTimeType
import com.numplates.nomera3.modules.chatrooms.ui.mapper.RoomsSettingsMapper
import com.numplates.nomera3.presentation.utils.makeEntity
import com.numplates.nomera3.presentation.utils.parseUniquename
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.rx2.asFlow
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import kotlin.math.max

interface RoomDataRepository {

    suspend fun getRooms(
        updatedAt: Long?,
        topTs: Long?,
        limit: Int
    ): List<DialogEntity>

    suspend fun getRooms(
        limit: Int,
        type: RoomTimeType
    ): List<DialogEntity>

    suspend fun getRoomsResponse(
        updatedAt: Long?,
        topTs: Long?,
        limit: Int
    ): GetRoomsResponse?

    suspend fun getRoom(roomId: Long?): DialogEntity?

    fun getRoomsSettings(): RoomsSettingsModel?

    suspend fun getRoomDataById(roomId: Long): DialogEntity?

    suspend fun getRoomDataByCompanionId(companionId: Long): DialogEntity?

    suspend fun updateDialog(dialogEntity: DialogEntity)

    suspend fun deleteMessageById(roomId: Long, messageId: String): Int

    suspend fun checkRoomOnServer(userId: Long, roomType: String): Pair<Boolean, Long>

    suspend fun getRoomsMaxUpdatedAt(): Long

    fun checkSwipeDownToShowChatSearchTooltipRequired(): Boolean

    fun confirmSwipeDownToShowChatTooltipShown()

    suspend fun sendRoomsToServer(roomIds: MutableList<Long>)

    suspend fun getUnsentMessages(): List<MessageEntity>

    fun incrementUnreadMessageCount(roomId: Long): Int

    fun saveRoomsIntoDb(dialogs: List<DialogEntity>, drafts: List<DraftModel>, unsentRooms: Set<Long>)

    fun insertSettingsDao()

    fun insertDialogs(newDialogs: List<DialogEntity>): List<Long>

    fun insertDialog(dialog: DialogEntity)

    suspend fun deleteRoomAndMessages(roomId: Long, isBoth: Boolean): Boolean

    fun deleteDialogByRoomId(roomId: Long)

    suspend fun markRoomDeleted(roomId: Long, isDeleted: Boolean): Int

    fun getDialogByRoomIdLive(roomId: Long?): LiveData<DialogEntity>

    fun getRoomByCompanionFlow(companionId: String): Flow<DialogEntity?>

    fun getRoomByCompanionFlow(companionId: Long): Flow<DialogEntity?>

    fun observeReloadDialogs(): Flow<Boolean>

    fun observeUnreadMessageCount(): LiveData<Int?>
}

@AppScope
class RoomDataRepositoryImpl @Inject constructor(
    private val dataStore: DataStore,
    private val roomsApi: RoomsApi,
    private val mainApi: ApiMain,
    private val appSettings: AppSettings,
    private val socket: WebSocketMainChannel,
    private val gson: Gson,
    private val roomsSettingsMapper: RoomsSettingsMapper
) : RoomDataRepository {

    override suspend fun getRooms(
        updatedAt: Long?,
        topTs: Long?,
        limit: Int
    ): List<DialogEntity> = withContext(Dispatchers.IO) {
        val response = try {
            roomsApi.getRooms(
                userId = appSettings.readUID(),
                updatedAt = updatedAt,
                topTs = topTs,
                userType = TYPE_USERCHAT,
                limit = limit
            ).data
        } catch (exception: Exception) {
            Timber.tag("RoomDataRepositoryImpl").e(exception)
            null
        }
        saveRoomsSettings(response)
        return@withContext response?.dialogList?.postProcessRooms() ?: emptyList()
    }

    override suspend fun getRooms(
        limit: Int,
        type: RoomTimeType
    ): List<DialogEntity>  = withContext(Dispatchers.IO) {
        var updatedAt: Long? = null
        var topTs: Long? = null
        when (type) {
            RoomTimeType.CURRENT_UPDATE -> {
                updatedAt = 0L
            }
            RoomTimeType.ROOMS_MAX_UPDATE -> {
                updatedAt = dataStore.dialogDao().getRoomsMaxUpdatedAt() ?: 0L
            }
            RoomTimeType.ROOMS_MIN_UPDATE -> {
                topTs = dataStore.dialogDao().getRoomsMinUpdatedAt() ?: 0L
            }
            RoomTimeType.LAST_UPDATE_DIALOG -> {
                updatedAt = dataStore.appSettingsDao().getLastUpdatedDialogs()
            }
            RoomTimeType.NONE -> Unit
        }
        val response = try {
            roomsApi.getRooms(
                userId = appSettings.readUID(),
                updatedAt = updatedAt,
                topTs = topTs,
                userType = TYPE_USERCHAT,
                limit = limit
            ).data
        } catch (exception: Exception) {
            Timber.tag("RoomDataRepositoryImpl").e(exception)
            null
        }
        saveRoomsSettings(response)
        return@withContext response?.dialogList?.postProcessRooms() ?: emptyList()
    }

    override fun getRoomsSettings(): RoomsSettingsModel? {
        return appSettings.readRoomsSettingsSettings(RoomsSettingsModel::class.java)
    }

    override suspend fun getRoomsResponse(
        updatedAt: Long?,
        topTs: Long?,
        limit: Int
    ): GetRoomsResponse? {
        val response = try {
            roomsApi.getRooms(
                userId = appSettings.readUID(),
                updatedAt = updatedAt,
                topTs = topTs,
                userType = TYPE_USERCHAT,
                limit = limit
            )
        } catch (exception: Exception) {
            Timber.tag("RoomDataRepositoryImpl").e(exception)
            null
        }
        return response?.data
    }

    override suspend fun getRoom(roomId: Long?): DialogEntity? = withContext(Dispatchers.IO) {
        return@withContext dataStore.dialogDao().getRoom(roomId)
    }

    override fun incrementUnreadMessageCount(roomId: Long) =
        dataStore.dialogDao().incrementUnreadMessageCount(roomId)

    override fun insertSettingsDao() =
        dataStore.appSettingsDao().insert(AppSettingsEntity(0, System.currentTimeMillis()))

    override fun insertDialogs(newDialogs: List<DialogEntity>): List<Long> = dataStore.dialogDao().insert(newDialogs)

    override suspend fun getRoomDataById(roomId: Long): DialogEntity? {
        return withContext(Dispatchers.IO) {
            dataStore.dialogDao().getDialogById(roomId).firstOrNull() ?: getRoomDataByIdFallback(roomId)
        }
    }

    override suspend fun getRoomDataByCompanionId(companionId: Long): DialogEntity? {
        return withContext(Dispatchers.IO) {
            dataStore.dialogDao().getDialogByCompanionId(companionId).firstOrNull()
        }
    }

    override suspend fun updateDialog(dialogEntity: DialogEntity) {
        withContext(Dispatchers.IO) {
            dataStore.dialogDao().insert(dialogEntity)
        }
    }

    override suspend fun deleteMessageById(roomId: Long, messageId: String): Int {
        return dataStore.messageDao().deleteMessageByIdSuspended(
            roomId = roomId,
            messageId = messageId
        )
    }

    /** If rooms data is not yet loaded into DB then get necessary data directly from backend.
     * Very inefficient since we cannot load room data by ID and have to load the whole list.
     */
    private suspend fun getRoomDataByIdFallback(roomId: Long): DialogEntity? {
        val response = try {
            roomsApi.getRooms(
                userId = appSettings.readUID(),
                userType = TYPE_USERCHAT,
                limit = PAGINATION_LOAD_ALL
            ).data
        } catch (exception: Exception) {
            Timber.tag("RoomDataRepositoryImpl").e(exception)
            null
        }
        return response?.dialogList?.find { it.roomId == roomId }
    }

    /**
     * @return Pair(isRoomExists, roomId)
     */
    @Throws(RuntimeException::class)
    override suspend fun checkRoomOnServer(
        userId: Long,
        roomType: String
    ): Pair<Boolean, Long> = withContext(Dispatchers.IO) {
        val payload = hashMapOf(
            ChatPayloadKeys.USER_ID.key to userId,
            ChatPayloadKeys.ROOM_TYPE.key to roomType
        )
        if (!socket.isConnected()) throw RuntimeException("ERROR connection to server. (Socket disconnected!)")
        val socketResponse = socket.pushCheckRoomSuspend(payload)
        return@withContext when (socketResponse.payload[ChatPayloadKeys.SOCKET_STATUS.key]) {
            STATUS_OK -> {
                val messageResponse = socketResponse.payload.makeEntity<ResponseWrapperWebSock<ResponseRoomId>>(gson)
                val roomId = messageResponse.response?.roomId
                if (roomId != null && roomId > 0) {
                    return@withContext Pair(true, roomId)
                } else {
                    return@withContext Pair(false, -1)
                }
            }
            STATUS_ERROR -> throw RuntimeException("Internal server error check_room")
            else -> Pair(false, -1)
        }
    }

    override suspend fun getRoomsMaxUpdatedAt(): Long = withContext(Dispatchers.IO) {
        return@withContext dataStore.dialogDao().getRoomsMaxUpdatedAt() ?: 0L
    }

    override fun checkSwipeDownToShowChatSearchTooltipRequired(): Boolean {
        return appSettings.isSwipeDownToShowTooltipRequired
    }

    override fun confirmSwipeDownToShowChatTooltipShown() {
        appSettings.isSwipeDownToShowTooltipRequired = false
    }

    override suspend fun sendRoomsToServer(rooms: MutableList<Long>) {
        runCatching {
            mainApi.markRoomsAsDelivered(hashMapOf("room_ids" to rooms)).data
        }.onFailure {
            Timber.tag("sendRoomsToServer").e(it)
        }
    }

    override suspend fun getUnsentMessages(): List<MessageEntity> {
        val lastUpdatedAtDB =
            dataStore.dialogDao().getRoomsMaxUpdatedAt() ?: 0L
        return dataStore.messageDao().getUnsentMessages(lastUpdatedAtDB)
    }

    /**
     * Store rooms data into DB
     */
    override fun saveRoomsIntoDb(dialogs: List<DialogEntity>, drafts: List<DraftModel>, unsentRooms: Set<Long>) {
        // Set last message updated at for ordering rooms by lastMessage createdAt
        val roomsToAdd = mutableListOf<DialogEntity>()
        val roomsToDelete = mutableListOf<DialogEntity>()
        dialogs.forEach { dlg ->
            val draft = drafts.firstOrNull { it.roomId == dlg.roomId }
            val lastMsgCreatedAt = dlg.lastMessage?.createdAt ?: 0
            val lastMessageUpdatedTs = if (draft != null) {
                max(draft.lastUpdatedTimestamp, lastMsgCreatedAt)
            } else {
                lastMsgCreatedAt
            }
            dlg.creatorUid = dlg.creator.userId ?: 0
            dlg.companionUid = dlg.companion.userId ?: 0
            dlg.lastMessageUpdatedAt = lastMessageUpdatedTs
            dlg.companionNotificationsOff = dlg.companion.settingsFlags?.notificationsOff

            // Refresh Unread counter bottom bar if Dialog deleted
            // Если удалено для группового чата остальные пользователи могут
            // просматривать старые сообщения, но не могут писать
            if (dlg.deleted && dlg.type != ROOM_TYPE_GROUP) {
                roomsToDelete.add(dlg)
            }

            // Удаленный дилог группового чата
            if (dlg.deleted && dlg.type == ROOM_TYPE_GROUP
                && dlg.creator.userId != appSettings.readUID()
            ) {
                dlg.deleted = false
            }

            // Insert / update room if not exists undelivered messages
            if (!unsentRooms.contains(dlg.roomId)) {
                roomsToAdd.add(dlg)
            }
        }
        val roomsMutated = roomsToAdd.map { it.copy(isHidden = it.companion.blacklistedByMe.toBoolean()) }
        dataStore.dialogDao().insert(roomsMutated)
        if (roomsToDelete.isNotEmpty()) {
            dataStore.dialogDao().delete(roomsToDelete)
        }
    }

    override fun insertDialog(dialog: DialogEntity) {
        dataStore.dialogDao().insert(dialog)
    }

    @Throws(RuntimeException::class)
    override suspend fun deleteRoomAndMessages(
        roomId: Long,
        isBoth: Boolean
    ): Boolean = withContext(Dispatchers.IO) {
        val payload = hashMapOf(
            ChatPayloadKeys.ROOM_ID.key to roomId,
            ChatPayloadKeys.IS_BOTH.key to isBoth
        )
        val socketResponse = socket.pushDeleteRoomSuspend(payload)
        when (socketResponse.payload[ChatPayloadKeys.SOCKET_STATUS.key]) {
            STATUS_OK -> {
                dataStore.dialogDao().deleteDialogByRoomId(roomId)
                dataStore.messageDao().deleteMessagesByRoomId(roomId)
                return@withContext true
            }
            STATUS_ERROR -> throw RuntimeException("Internal server error delete_room")
            else -> throw RuntimeException("Internal server error delete_room")
        }
    }

    override fun deleteDialogByRoomId(roomId: Long) {
        dataStore.dialogDao().deleteDialogByRoomId(roomId)
    }

    override suspend fun markRoomDeleted(roomId: Long, isDeleted: Boolean): Int = withContext(Dispatchers.IO) {
        return@withContext dataStore.dialogDao().markRoomDeleted(roomId, isDeleted)
    }

    override fun getDialogByRoomIdLive(roomId: Long?) =
        dataStore.dialogDao().getDialogByRoomIdLive(roomId)

    override fun getRoomByCompanionFlow(companionId: String): Flow<DialogEntity?> =
        dataStore.dialogDao().getRoomByCompanionFlow(companionId)

    override fun getRoomByCompanionFlow(companionId: Long): Flow<DialogEntity?> {
        return dataStore.dialogDao().getRoomByCompanionFlow(companionId)
    }

    override fun observeReloadDialogs(): Flow<Boolean> {
        return socket.observeReloadDialogs().asFlow().map { true }
    }

    override fun observeUnreadMessageCount(): LiveData<Int?> =
        dataStore.dialogDao().liveCountUnreadMessages()

    private fun List<DialogEntity>.postProcessRooms(): List<DialogEntity> {
        return this
            .filter { it.lastMessage != null }
            .map { room ->
                val lastMessageContent = parseUniquename(
                    input = room.lastMessage?.content,
                    tags = room.lastMessage?.tags
                ).text ?: String.empty()
                room.lastMessage?.content = lastMessageContent
                room
            }
    }

    private fun saveRoomsSettings(response: GetRoomsResponse?) {
        val settings = roomsSettingsMapper.mapRoomsSettings(response)
        appSettings.writeRoomsSettings(settings)
    }

    companion object {
        private const val PAGINATION_LOAD_ALL = 0
        private const val TYPE_USERCHAT = "UserChat"
    }
}
