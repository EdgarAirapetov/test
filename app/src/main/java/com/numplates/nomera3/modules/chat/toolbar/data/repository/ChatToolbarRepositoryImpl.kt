package com.numplates.nomera3.modules.chat.toolbar.data.repository

import com.google.gson.Gson
import com.meera.core.di.scopes.AppScope
import com.meera.core.extensions.empty
import com.meera.core.network.websocket.WebSocketMainChannel
import com.meera.db.DataStore
import com.meera.db.models.dialog.UserChat
import com.numplates.nomera3.data.newmessenger.response.ChatUsers
import com.numplates.nomera3.data.newmessenger.response.ResponseWrapperWebSock
import com.numplates.nomera3.modules.chat.toolbar.data.api.ChatUserApi
import com.numplates.nomera3.modules.chat.toolbar.data.entity.ChatUserTypingEntity
import com.numplates.nomera3.modules.chat.toolbar.data.entity.OnlineChatStatusEntity
import com.numplates.nomera3.presentation.utils.makeEntity
import io.reactivex.Observable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.phoenixframework.Message
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject

@AppScope
class ChatToolbarRepositoryImpl @Inject constructor(
    private val userApi: ChatUserApi,
    private val socket: WebSocketMainChannel,
    private val gson: Gson,
    private val dataStore: DataStore
) : ChatToolbarRepository {


    override fun updateUserDataObserver(): Observable<Message> =
            socket.observeUpdateUser()

    override suspend fun getUserInfo(userIds: List<Long>): List<UserChat> {
        return withContext(Dispatchers.IO) {
            val payload = hashMapOf(
                "user_type" to "UserChat",
                "user_id" to userIds
            )
            val message = socket.getUserInfo(payload)
            val result = message.payload.makeEntity<ResponseWrapperWebSock<ChatUsers>>(gson)
            result?.response?.users ?: emptyList()
        }
    }

    @Throws(IOException::class)
    override suspend fun getUserInfo(userId: Long): List<UserChat> {
        val result = userApi.getUserInfo(
            userId = userId,
            userType = "UserChat",
            version = String.empty()
        )
        if (result.data != null) {
            return result.data.users
        } else {
            throw IOException("Get user info network error! (${result.err})")
        }
    }

    /**
     * Обновление компаньона в конкретной Rooms БД
     * если это чат типа Dialog
     */
    override suspend fun updateCompanionUser(
            roomId: Long,
            user: UserChat,
            success: (Boolean) -> Unit,
            fail: (Exception) -> Unit
    ) {
        try {
            dataStore.dialogDao().updateCompanionUser(roomId, user)
            success(true)
        } catch (e: Exception) {
            Timber.e("Internal Db error:$e")
            fail(e)
        }
    }

    override fun observeOnlineStatus(roomId: Long?): Flow<OnlineChatStatusEntity> {
        return socket.observeOnlineFlow().map { phoenixMessage ->
            phoenixMessage.payload.makeEntity<OnlineChatStatusEntity>(gson)
        }.flowOn(Dispatchers.IO)
    }

    override fun observeTyping(): Flow<ChatUserTypingEntity> {
        return socket.observeTypingFlow().map { phoenixMessage ->
            phoenixMessage.payload.makeEntity<ChatUserTypingEntity>(gson)
        }.flowOn(Dispatchers.IO)
    }

    override suspend fun updateDialogCompanionNotificationsNetwork(
        userId: Long,
        isMuted: Boolean,
        success: (Boolean) -> Unit,
        fail: (Exception) -> Unit
    ) {
        try {
            val response = userApi.setMessageNotificationPermission(
                userId = userId,
                value = isMuted
            )
            if (response.data != null) {
                success(true)
            } else {
                fail(IOException("Message notification permission ERROR"))
            }
        } catch (e: Exception) {
            Timber.e(e)
            fail(e)
        }
    }

    @Suppress("UNUSED_VARIABLE")
    override suspend fun muteNotificationsGroupChat(
        roomId: Long,
        success: (Boolean) -> Unit,
        fail: (Exception) -> Unit
    ) = withContext(Dispatchers.IO) {
        try {
            val payload = hashMapOf("room_id" to roomId)
            val message = socket.muteRoomSuspend(payload)
            dataStore.dialogDao().updateMutedRoomState(roomId, true)
            success(true)
        } catch (e: Exception) {
            Timber.e(e)
            fail(e)
        }
    }

    @Suppress("UNUSED_VARIABLE")
    override suspend fun unmuteNotificationsGroupChat(
        roomId: Long,
        success: (Boolean) -> Unit,
        fail: (Exception) -> Unit
    ) = withContext(Dispatchers.IO) {
        try {
            val payload = hashMapOf("room_id" to roomId)
            val message = socket.unmuteRoomSuspend(payload)
            dataStore.dialogDao().updateMutedRoomState(roomId, false)
            success(true)
        } catch (e: Exception) {
            Timber.e(e)
            fail(e)
        }
    }

    override suspend fun setCallPrivacyForUser(
        userId: Long,
        isSet: Boolean,
        success: (Boolean) -> Unit,
        fail: (Exception) -> Unit
    ) {
        try {
            val payload = hashMapOf(
                "user_id" to userId,
                "value" to isSet
            )
            val message = socket.pushSetCallPrivacyForUserSuspend(payload)
            // Timber.e("SET call privacy for user RESPONSE:${message.toJsonPrettyPrinting()}")
            success(true)
        } catch (e: Exception) {
            Timber.e(e)
            fail(e)
        }
    }
}
