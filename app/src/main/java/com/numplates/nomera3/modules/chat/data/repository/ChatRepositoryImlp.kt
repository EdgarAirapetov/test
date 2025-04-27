package com.numplates.nomera3.modules.chat.data.repository

import com.meera.core.extensions.toInt
import com.meera.core.network.websocket.ConnectionStatus
import com.meera.core.network.websocket.WebSocketMainChannel
import com.meera.core.preferences.AppSettings
import com.meera.db.DataStore
import com.meera.db.models.chatmembers.ChatMember
import com.numplates.nomera3.data.newmessenger.ROOM_TYPE_DIALOG
import com.numplates.nomera3.data.newmessenger.ROOM_TYPE_GROUP
import com.numplates.nomera3.data.websocket.STATUS_ERROR
import com.numplates.nomera3.data.websocket.STATUS_OK
import com.numplates.nomera3.modules.chat.ChatPayloadKeys
import com.numplates.nomera3.modules.chat.domain.ChatRepository
import com.numplates.nomera3.modules.chat.helpers.chatinit.ChatInitProfileData
import io.reactivex.subjects.BehaviorSubject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ChatRepositoryImlp @Inject constructor(
    private val dataStore: DataStore,
    private val socket: WebSocketMainChannel,
    private val appSettings: AppSettings
) : ChatRepository {

    override fun getAllMembersLive(roomId: Long?) =
        dataStore.daoChatMembers().getAllMembersLive(roomId)

    override fun getAllMembers(roomId: Long?): List<ChatMember> =
        dataStore.daoChatMembers().getAllMembers(roomId)

    @Throws(RuntimeException::class)
    override suspend fun removeChatUser(roomId: Long, userId: Long) = withContext(Dispatchers.IO) {
        val payload = mapOf(
            ChatPayloadKeys.USER_ID.key to userId,
            ChatPayloadKeys.ROOM_ID.key to roomId
        )
        val socketResponse = socket.pushRemoveUserSuspend(payload)
        when (socketResponse.payload[ChatPayloadKeys.SOCKET_STATUS.key]) {
            STATUS_OK -> {
                dataStore.daoChatMembers().deleteMemberById(userId, roomId)
            }

            STATUS_ERROR -> throw RuntimeException("Internal server error remove_chat_user")
        }
    }

    override suspend fun deleteChatMemberInDb(
        userId: Long,
        roomId: Long
    ) = withContext(Dispatchers.IO) {
        return@withContext dataStore.daoChatMembers().deleteMemberById(userId, roomId)
    }

    @Throws(RuntimeException::class)
    override suspend fun subscribeRoom(roomId: Long): Boolean = withContext(Dispatchers.IO) {
        val payload = mapOf(ChatPayloadKeys.ROOM_ID.key to roomId)
        val socketResponse = socket.pushRoomSubscribe(payload)
        return@withContext when (socketResponse.payload[ChatPayloadKeys.SOCKET_STATUS.key]) {
            STATUS_OK -> true
            else -> throw RuntimeException("Server error subscribe_room")
        }
    }

    override suspend fun changeRoomMuteState(roomId: Long, isMuted: Boolean) = withContext(Dispatchers.IO) {
        val room = dataStore.dialogDao().getRoom(roomId) ?: return@withContext
        if (room.type == ROOM_TYPE_GROUP) {
            val payload = hashMapOf("room_id" to roomId)
            if (isMuted) {
                socket.muteRoomSuspend(payload)
            } else {
                socket.unmuteRoomSuspend(payload)
            }
            dataStore.dialogDao().update(room.copy(isMuted = isMuted))
        } else if (room.type == ROOM_TYPE_DIALOG) {
            val payload = hashMapOf(
                "user_id" to room.companion.userId,
                "value" to isMuted
            )
            socket.pushMessageNotificationPrivacySuspend(payload)
            val updatedSettings = room.companion.settingsFlags?.copy(notificationsOff = isMuted.toInt())
            val updatedCompanion = room.companion.copy(settingsFlags = updatedSettings)
            dataStore.dialogDao().update(room.copy(companion = updatedCompanion))
        }
    }

    override fun isWebSocketConnected(): Boolean =
        socket.isInitialized()
            && socket.isConnected()
            && socket.isChannelJoined()

    override fun listenSocketStatus(): BehaviorSubject<ConnectionStatus> {
        return socket.publishSocketStatus
    }

    override fun cacheCompanionUserForChatInit(user: ChatInitProfileData?): ChatInitProfileData? {
        return if (user != null && user.mainInfo.userId != appSettings.readUID()) {
            appSettings.writeChatInitCompanionUser(user)
            user
        } else {
            appSettings.readChatInitCompanionUser(ChatInitProfileData::class.java)
        }
    }
}
