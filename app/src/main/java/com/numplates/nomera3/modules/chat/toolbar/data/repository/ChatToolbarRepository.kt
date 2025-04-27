package com.numplates.nomera3.modules.chat.toolbar.data.repository

import com.meera.db.models.dialog.UserChat
import com.numplates.nomera3.modules.chat.toolbar.data.entity.ChatUserTypingEntity
import com.numplates.nomera3.modules.chat.toolbar.data.entity.OnlineChatStatusEntity
import io.reactivex.Observable
import kotlinx.coroutines.flow.Flow
import org.phoenixframework.Message


interface ChatToolbarRepository {

    fun updateUserDataObserver(): Observable<Message>

    suspend fun getUserInfo(userIds: List<Long>) : List<UserChat>

    suspend fun getUserInfo(userId: Long): List<UserChat>

    suspend fun updateCompanionUser(
        roomId: Long,
        user: UserChat,
        success: (Boolean) -> Unit,
        fail: (Exception) -> Unit
    )

    fun observeOnlineStatus(roomId: Long?): Flow<OnlineChatStatusEntity>

    fun observeTyping(): Flow<ChatUserTypingEntity>

    suspend fun updateDialogCompanionNotificationsNetwork(
        userId: Long,
        isMuted: Boolean,
        success: (Boolean) -> Unit,
        fail: (Exception) -> Unit
    )

    suspend fun muteNotificationsGroupChat(
        roomId: Long,
        success: (Boolean) -> Unit,
        fail: (Exception) -> Unit
    )

    suspend fun unmuteNotificationsGroupChat(
        roomId: Long,
        success: (Boolean) -> Unit,
        fail: (Exception) -> Unit
    )

    suspend fun setCallPrivacyForUser(
        userId: Long,
        isSet: Boolean,
        success: (Boolean) -> Unit,
        fail: (Exception) -> Unit
    )
}
