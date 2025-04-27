package com.numplates.nomera3.modules.chat.domain


import android.net.Uri
import com.meera.db.models.dialog.LastMessage
import com.meera.db.models.message.MessageEntity
import com.numplates.nomera3.modules.chat.domain.params.MessagePaginationDirection
import com.numplates.nomera3.modules.chat.helpers.sendmessage.models.SendMessageEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import java.io.File

interface ChatMessageRepository {

    val eventsFlow: SharedFlow<SendMessageEvent>

    suspend fun saveMessageIntoDb(
        message: MessageEntity,
        shouldEmmitEvent: Boolean
    ): Long

    fun updateLastMessage(
        message: MessageEntity,
        lastMessage: LastMessage
    )

    fun updateMessageSentStatus(
        messageId: String,
        isSent: Boolean,
        isShowLoadingProgress: Boolean
    )

    suspend fun updateBadgeStatus(
        roomId: Long,
        needToShowBadge: Boolean
    ): Int

    suspend fun updateLastUnreadMessageTs(
        roomId: Long,
        timestamp: Long
    ): Int

    suspend fun getMessages(
        roomId: Long,
        lastUpdatedAtMessages: Long,
        direction: MessagePaginationDirection,
    ): List<MessageEntity>

    @Throws(RuntimeException::class)
    suspend fun getMessagesAndInsertInDb(
        roomId: Long,
        timeStamp: Long,
        limit: Int,
        direction: MessagePaginationDirection,
        isRoomChatRequest: Boolean,
        needToShowUnreadMessagesSeparator: Boolean,
        isInitialRequest: Boolean,
        userType: String
    ): List<MessageEntity>

    suspend fun getChatMessageListPosition(message: MessageEntity): Int

    suspend fun getMessageById(messageId: String): MessageEntity?

    suspend fun updateMessageEditingStatus(messageId: String, isEditing: Boolean)

    suspend fun updateMessageData(message: MessageEntity)

    suspend fun getFileForContentUri(media: Uri, label: String): File?

    fun downloadVoiceMessage(message: MessageEntity, externalFilesDir: File?): Flow<Int>

    fun downloadVoiceMessage(url: String, roomId: Long, externalFilesDir: File?): Flow<Int>

    suspend fun readMessageNetwork(roomId: Long, messageIds: List<String>): Boolean

    suspend fun sendTyping(roomId: Long, type: String): Boolean

    suspend fun deleteMessageNetwork(roomId: Long, messageId: String, isBoth: Boolean): Boolean

    fun observeIncomingMessage(): Flow<MessageEntity>

    suspend fun getResendProgressMessages(roomId: Long): List<MessageEntity>?

}
