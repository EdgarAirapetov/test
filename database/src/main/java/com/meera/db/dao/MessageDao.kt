package com.meera.db.dao

import androidx.paging.DataSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import com.meera.db.models.message.MessageEntity
import com.meera.db.models.message.MessageEntityNetwork
import io.reactivex.Observable
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {

    @Query("SELECT count(*) FROM messages_new WHERE room_id = :roomId")
    fun observeCountMessages(roomId: Long): Flow<Long>

    @Query("SELECT * FROM messages_new WHERE room_id = :roomId ORDER BY created_at DESC")
    fun messagesByRoom(roomId: Long): DataSource.Factory<Int, MessageEntity>

    @Query("SELECT * FROM messages_new WHERE room_id = :roomId ORDER BY created_at DESC")
    fun messagesByRoomFlow(roomId: Long): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages_new WHERE room_id = :roomId ORDER BY created_at DESC LIMIT :limit OFFSET :offset")
    fun getMessagesLimitOffset(roomId: Long, limit: Int, offset: Int): List<MessageEntity>

    @Query("DELETE FROM messages_new WHERE room_id = :roomId AND created_at < :createdAt")
    fun deleteMessagesByCreatedAtDESC(roomId: Long, createdAt: Long)

    @Query("SELECT * FROM messages_new WHERE room_id = :roomId ORDER BY updated_at DESC LIMIT 1")
    fun lastMessageUpdated(roomId: Long): Observable<MessageEntity>

    @Query("SELECT count(*) FROM messages_new WHERE room_id = :roomId AND creator_uid != :creatorUid AND readed = :isRead")    // readed = :isRead
    fun allUnreadMessageCountRx(roomId: Long?, creatorUid: Long, isRead: Boolean): Observable<Long>

    @Query("SELECT count(*) FROM messages_new WHERE room_id = :roomId AND readed = 0")
    fun getUnreadMessageCount(roomId: Long?): Int

    @Query("SELECT count(*) FROM messages_new WHERE room_id = :roomId")
    fun getMessageCount(roomId: Long?): Int

    @Query("SELECT * FROM messages_new WHERE room_id = :roomId AND creator_uid != :creatorUid AND readed == 0")
    fun allUnreadIncomingMessages(roomId: Long, creatorUid: Long): Observable<List<MessageEntity>>

    @Query("SELECT unread_message_count FROM dialogs WHERE room_id = :roomId")
    fun roomUnreadMessageCountFlow(roomId: Long?): Flow<Long?>

    // find any unsent messages for show unsent notification
    @Query("SELECT * FROM messages_new WHERE room_id != 0 AND sent = 0 AND is_resend_available = 1 AND deleted = 0")
    fun getAllUnsentMessages(): List<MessageEntity?>

    @Query("SELECT * FROM messages_new WHERE message_id = :messageId AND sent = 0 AND deleted = 0")
    fun getSingleUnsentMessage(messageId: String): MessageEntity

    @Query("SELECT * FROM messages_new WHERE room_id = :roomId AND sent = 0 AND is_resend_available = 1 AND deleted = 0")
    fun getAllUnsentMessages(roomId: Long): List<MessageEntity>

    @Query("SELECT count(*) FROM messages_new WHERE room_id = :roomId AND sent = 0 AND is_resend_available = 1 AND deleted = 0")
    fun getCountAllUnsentMessages(roomId: Long?): Int

    @Query("SELECT * FROM messages_new WHERE room_id = :roomId AND is_resend_progress = 1 AND is_resend_available = 1 AND deleted = 0")
    fun getResendProgressMessages(roomId: Long?): List<MessageEntity>?

    @Query("UPDATE dialogs SET unread_message_count = unread_message_count - 1 WHERE room_id = :roomId")
    fun decrementUnreadMessageCountInRoom(roomId: Long?): Int

    @Query("SELECT * FROM messages_new WHERE room_id = :roomId ORDER BY created_at ASC")
    fun getFirstMessage(roomId: Long): MessageEntity?

    @Query("SELECT * FROM messages_new WHERE room_id = :roomId AND deleted = 0 ORDER BY created_at DESC")
    fun getLastUndeletedMessage(roomId: Long?): MessageEntity?

    @Query("SELECT * FROM messages_new WHERE message_id = :messageId")
    fun getMessageById(messageId: String?): MessageEntity?

    @Query("SELECT * FROM messages_new WHERE message_id = :messageId")
    suspend fun getMessageByIdSuspend(messageId: String?): MessageEntity?

    @Query("SELECT count(*) FROM messages_new WHERE room_id = :roomId AND show_unread_divider = 1")
    fun getCountShowUnreadDivider(roomId: Long?): Int

    @Query("SELECT * FROM messages_new WHERE sent == 0 AND created_at > :lastUpdatedAtDb")
    fun getUnsentMessages(lastUpdatedAtDb: Long): List<MessageEntity>

    @Query("UPDATE messages_new SET show_unread_divider = 0")
    fun removeUnreadDivider(): Int

    @Query("SELECT * FROM messages_new WHERE room_id = :roomId AND created_at > :createdAt")
    fun getNextMessages(roomId: Long, createdAt: Long): List<MessageEntity?>


    @Query("UPDATE messages_new SET is_expanded_recognized_text = :isExpanded WHERE message_id = :messageId")
    fun updateExpandVoiceMessageTextState(messageId: String, isExpanded: Boolean): Int

    @Query("UPDATE messages_new SET is_expanded_recognized_text = :isExpanded WHERE room_id = :roomId")
    fun updateExpandVoiceMessageTextState(roomId: Long?, isExpanded: Boolean): Int

    @Query("UPDATE messages_new SET is_expanded_recognized_text = :isExpandedRecognizedText, refresh_message_item = refresh_message_item + 1 AND message_id = :messageId")
    fun updateRefreshVoiceMessage(messageId: String?, isExpandedRecognizedText: Boolean?): Int

    @Deprecated("Use flow callback")
    @Query("SELECT * FROM messages_new WHERE room_id = :roomId AND type = 'event' ORDER BY created_at ASC")
    fun getEventMessageCodes(roomId: Long): Observable<List<MessageEntity>>

    @Query("SELECT * FROM messages_new WHERE room_id = :roomId AND type = 'event' ORDER BY created_at ASC")
    fun getEventMessageCodesFlow(roomId: Long): Flow<List<MessageEntity>>

    @Query("SELECT MAX(updated_at) FROM messages_new WHERE room_id = :roomId")
    fun getLastMessageUpdatedTime(roomId: Long?): Long

    @Query("UPDATE messages_new SET readed = :isRead WHERE room_id = :roomId AND message_id = :messageId")
    fun updateMessageToRead(roomId: Long?, messageId: String, isRead: Boolean): Int

    @Query("UPDATE messages_new SET readed = :isRead WHERE room_id = :roomId")
    fun updateAllMessagesToRead(roomId: Long?, isRead: Boolean): Int

    @Query("UPDATE messages_new SET is_resend_progress = :showResendProgress")
    fun updateAllResendProgressStatus(showResendProgress: Boolean)

    @Query("UPDATE messages_new SET is_resend_progress = :showResendProgress WHERE room_id = :roomId")
    fun updateAllResendProgressStatusByRoomId(roomId: Long?, showResendProgress: Boolean)

    @Query("UPDATE messages_new SET is_editing_progress = :showEditingProgress WHERE message_id = :messageId")
    suspend fun updateProgressStatusByMessageId(messageId: String, showEditingProgress: Boolean)

    @Query("SELECT * FROM messages_new WHERE room_id = :roomId AND created_at < :createdAtCurrent ORDER BY created_at DESC LIMIT 1")
    fun getPreviousMessage(roomId: Long, createdAtCurrent: Long): MessageEntity?

    @Query("SELECT count(*) FROM messages_new WHERE room_id = :roomId AND created_at > :createdAt")
    suspend fun getMessagePositionDb(roomId: Long?, createdAt: Long?): Int

    @Query("UPDATE messages_new SET is_show_avatar = 0 WHERE room_id = :roomId AND message_id = :messageId")
    fun setInvisiblePreviousAvatar(roomId: Long, messageId: String): Int

    @Query("UPDATE messages_new SET refresh_message_item = refresh_message_item + 1 WHERE room_id = :roomId AND message_id = :messageId")
    fun refreshMessageItem(roomId: Long, messageId: String): Int

    @Query("UPDATE messages_new SET refresh_message_item = refresh_message_item + 1 WHERE message_id = :messageId")
    fun refreshMessageItem(messageId: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(message: MessageEntity): Long

    @Query("UPDATE messages_new SET parent_message = null WHERE parent_id = :parentId")
    fun setParentNullById(parentId: String): Int

    @Query("UPDATE messages_new SET is_show_image_blur_chat_request = :isShowBlur WHERE room_id = :roomId AND creator_userId != :creatorUid")
    fun setBlurChatRequestAllMessages(roomId: Long, creatorUid: Long, isShowBlur: Boolean): Int

    @Query("UPDATE messages_new SET is_show_image_blur_chat_request = :isShowBlur WHERE message_id = :messageId")
    fun updateChatRequestImageBlur(messageId: String?, isShowBlur: Boolean): Int

    @Query("UPDATE messages_new SET sent = :isSent, is_show_loading_progress = :isShowLoadingProgress WHERE message_id = :messageId")
    fun updateSentStatus(messageId: String, isSent: Boolean, isShowLoadingProgress: Boolean): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(messages: List<MessageEntity>): List<Long>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertWithoutReplace(messages: List<MessageEntity>): List<Long>

    @Update
    fun update(message: MessageEntity): Int

    @Update
    suspend fun updateSuspend(message: MessageEntity): Int

    /**
     * Метод частичной вставки/обновления полей указанных в MessageEntityNetwork
     * Example:
     * val networkMessages = messengerEntityMapper.mapToNetworkMessages(messagesResponse)
     * dataStore.messageDao().upsertFromNetworkMessages(networkMessages)
     */
    @Upsert(entity = MessageEntity::class)
    suspend fun upsertFromNetworkMessages(networkMessages: List<MessageEntityNetwork>): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE, entity = MessageEntity::class)
    suspend fun insertFromNetworkMessages(networkMessages: List<MessageEntityNetwork>): List<Long>

    @Update(entity = MessageEntity::class)
    suspend fun updateFromNetworkMessage(networkMessage: List<MessageEntityNetwork>): Int

    @Query("DELETE FROM messages_new WHERE room_id = :roomId")
    fun deleteMessagesByRoomId(roomId: Long?): Int

    @Query("DELETE FROM messages_new WHERE room_id = :roomId AND message_id = :messageId")
    suspend fun deleteMessageByIdSuspended(roomId: Long, messageId: String): Int

    @Query("DELETE FROM messages_new WHERE room_id = :roomId AND creator_uid != :myUid AND readed == 0")
    fun deleteUnreadIncomingMessages(roomId: Long?, myUid: Long?): Int

    @Query("SELECT count(*) FROM messages_new WHERE room_id = :roomId")
    fun getMessagesCountForRoomId(roomId: Long?): Int

    @Query("UPDATE messages_new SET is_play_voice_message = 0 WHERE room_id = :roomId AND is_play_voice_message = 1")
    fun setIsPlayingMessageFalse(roomId: Long): Int

    @Query("DELETE FROM messages_new")
    fun purge()
}
