package com.numplates.nomera3.modules.chat.domain

import com.meera.db.models.message.MessageEntity
import kotlinx.coroutines.flow.Flow

interface ChatPersistDbRepository {

    suspend fun getLastMessageUpdatedTime(roomId: Long): Long

    fun observeEventMessages(roomId: Long): Flow<List<MessageEntity>>

    fun observeCountMessages(roomId: Long): Flow<Long>

    suspend fun readAndDecrementMessageUseCase(roomId: Long, messageId: String)

    fun observeUnreadMessageCounter(roomId: Long?): Flow<Long?>

    suspend fun updateMessage(message: MessageEntity): Int

    suspend fun refreshMessage(roomId: Long, messageId: String): Int

    suspend fun refreshMessage(messageId: String): Int

    suspend fun refreshFirstMessage(roomId: Long): Int

    suspend fun removeUnreadDivider(): Int

    suspend fun updateRoomAsRead(roomId: Long): Int

    suspend fun getUnsentMessageCount(roomId: Long): Int

    suspend fun deleteUnsentMessage(message: MessageEntity)

    suspend fun countAllUnsentMessages(roomId: Long): Int

    suspend fun getNextMessages(roomId: Long, createdAt: Long): List<MessageEntity?>

    suspend fun updateIsExpandedVoiceMessage(messageId: String, isExpanded: Boolean): Int

    suspend fun updateIsExpandedVoiceMessages(roomId: Long?, isExpanded: Boolean): Int

    suspend fun updateAndRefreshIsExpandedVoiceMessage(messageId: String?, isExpanded: Boolean?): Int

    suspend fun updateVoiceMessageAsStopped(roomId: Long): Int
}
