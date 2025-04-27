package com.numplates.nomera3.modules.chat.data.repository

import com.meera.core.di.scopes.AppScope
import com.meera.core.preferences.AppSettings
import com.meera.db.DataStore
import com.meera.db.models.message.MessageEntity
import com.numplates.nomera3.modules.chat.domain.ChatPersistDbRepository
import com.numplates.nomera3.modules.chat.helpers.resolveMessageType
import com.numplates.nomera3.modules.chat.helpers.toRoomLastMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AppScope
class ChatPersistDbRepositoryImpl @Inject constructor(
    private val dataStore: DataStore,
    private val appSettings: AppSettings
): ChatPersistDbRepository {

    override suspend fun getLastMessageUpdatedTime(roomId: Long): Long = withContext(Dispatchers.IO) {
        return@withContext dataStore.messageDao().getLastMessageUpdatedTime(roomId)
    }

    override fun observeEventMessages(roomId: Long): Flow<List<MessageEntity>> {
        return dataStore.messageDao().getEventMessageCodesFlow(roomId)
    }

    override fun observeCountMessages(roomId: Long): Flow<Long> {
        return dataStore.messageDao().observeCountMessages(roomId)
    }

    override suspend fun readAndDecrementMessageUseCase(roomId: Long, messageId: String) {
        dataStore.messageDao().decrementUnreadMessageCountInRoom(roomId)
        dataStore.messageDao().updateMessageToRead(roomId = roomId, messageId = messageId, isRead = true)
    }

    override fun observeUnreadMessageCounter(roomId: Long?): Flow<Long?> {
        return dataStore.messageDao().roomUnreadMessageCountFlow(roomId)
    }

    override suspend fun updateMessage(message: MessageEntity): Int = withContext(Dispatchers.IO){
        return@withContext dataStore.messageDao().update(message)
    }

    override suspend fun refreshMessage(roomId: Long, messageId: String): Int = withContext(Dispatchers.IO) {
        return@withContext dataStore.messageDao().refreshMessageItem(roomId, messageId)
    }

    override suspend fun refreshMessage(messageId: String): Int = withContext(Dispatchers.IO) {
        return@withContext dataStore.messageDao().refreshMessageItem(messageId)
    }

    override suspend fun refreshFirstMessage(roomId: Long): Int = withContext(Dispatchers.IO) {
        val firstMessage = dataStore.messageDao().getFirstMessage(roomId)
        if (firstMessage != null) {
            return@withContext dataStore.messageDao().refreshMessageItem(roomId, firstMessage.msgId)
        } else {
            return@withContext 0
        }
    }

    override suspend fun removeUnreadDivider(): Int = withContext(Dispatchers.IO) {
        return@withContext dataStore.messageDao().removeUnreadDivider()
    }

    override suspend fun updateRoomAsRead(roomId: Long): Int = withContext(Dispatchers.IO) {
        dataStore.dialogDao().updateUnreadMessageCount(roomId, 0)
        dataStore.messageDao().updateAllMessagesToRead(roomId, true)
    }

    override suspend fun getUnsentMessageCount(roomId: Long): Int = withContext(Dispatchers.IO) {
        return@withContext dataStore.messageDao().getCountAllUnsentMessages(roomId)
    }

    /**
     * Помечаем неотправленное сообщение как удалённое, когда недоступен интернет
     * и мы не можем получить ответ от сервера
     */
    override suspend fun deleteUnsentMessage(message: MessageEntity): Unit = withContext(Dispatchers.IO) {
        message.apply {
            deleted = true
            itemType = resolveMessageType(
                creatorId = message.creator?.userId,
                attachment = message.attachment,
                attachments = message.attachments,
                deleted = deleted,
                eventCode = message.eventCode,
                type = message.type,
                myUid = appSettings.readUID()
            )
            dataStore.messageDao().insert(message)
            dataStore.messageDao().refreshMessageItem(message.roomId, message.msgId)
            updateLastMessageIfDeleted(message.roomId)
        }
    }

    override suspend fun countAllUnsentMessages(roomId: Long): Int = withContext(Dispatchers.IO) {
        return@withContext dataStore.messageDao().getCountAllUnsentMessages(roomId)
    }

    override suspend fun getNextMessages(
        roomId: Long,
        createdAt: Long
    ): List<MessageEntity?> = withContext(Dispatchers.IO) {
        return@withContext dataStore.messageDao().getNextMessages(roomId, createdAt)
    }

    override suspend fun updateIsExpandedVoiceMessage(
        messageId: String,
        isExpanded: Boolean
    ): Int = withContext(Dispatchers.IO) {
        return@withContext dataStore.messageDao().updateExpandVoiceMessageTextState(messageId, isExpanded)
    }

    override suspend fun updateIsExpandedVoiceMessages(
        roomId: Long?,
        isExpanded: Boolean
    ): Int = withContext(Dispatchers.IO) {
        return@withContext dataStore.messageDao().updateExpandVoiceMessageTextState(roomId, isExpanded)
    }

    override suspend fun updateAndRefreshIsExpandedVoiceMessage(
        messageId: String?,
        isExpanded: Boolean?
    ): Int = withContext(Dispatchers.IO) {
        return@withContext dataStore.messageDao().updateRefreshVoiceMessage(messageId, isExpanded)
    }

    override suspend fun updateVoiceMessageAsStopped(roomId: Long): Int = withContext(Dispatchers.IO) {
        return@withContext dataStore.messageDao().setIsPlayingMessageFalse(roomId)
    }

    /**
     * Обновить последнее сообщение в комнате, чтобы последним
     * стояло неудалённое сообщение
     */
    private fun updateLastMessageIfDeleted(roomId: Long) {
        val prevMessage = dataStore.messageDao().getLastUndeletedMessage(roomId)
        val roomLastMessage = prevMessage?.toRoomLastMessage()
        dataStore.dialogDao().updateLastMessage(roomId, roomLastMessage, roomLastMessage?.sent)
    }

}
