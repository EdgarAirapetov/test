package com.numplates.nomera3.modules.chat.domain.interactors

import com.meera.db.models.message.MessageEntity
import com.numplates.nomera3.modules.chat.domain.params.MessagePaginationDirection
import com.numplates.nomera3.modules.chat.domain.usecases.GetMessageByIdUseCase
import com.numplates.nomera3.modules.chat.domain.usecases.messages.CountAllUnsentMessagesUseCase
import com.numplates.nomera3.modules.chat.domain.usecases.messages.DeleteMessageNetworkUseCase
import com.numplates.nomera3.modules.chat.domain.usecases.messages.DeleteUnsentMessageUseCase
import com.numplates.nomera3.modules.chat.domain.usecases.messages.GetLastMessageUpdatedTimeUseCase
import com.numplates.nomera3.modules.chat.domain.usecases.messages.GetMessagesUseCase
import com.numplates.nomera3.modules.chat.domain.usecases.messages.GetNextMessagesUseCase
import com.numplates.nomera3.modules.chat.domain.usecases.messages.GetResendProgressMessageUseCase
import com.numplates.nomera3.modules.chat.domain.usecases.messages.GetUnsentMessageCountUseCase
import com.numplates.nomera3.modules.chat.domain.usecases.messages.ObserveCountMessagesUseCase
import com.numplates.nomera3.modules.chat.domain.usecases.messages.ObserveEventMessagesUseCase
import com.numplates.nomera3.modules.chat.domain.usecases.messages.ObserveIncomingMessageUseCase
import com.numplates.nomera3.modules.chat.domain.usecases.messages.ObserveUnreadMessageCounterUseCase
import com.numplates.nomera3.modules.chat.domain.usecases.messages.ReadAndDecrementMessageUseCase
import com.numplates.nomera3.modules.chat.domain.usecases.messages.ReadMessageNetworkUseCase
import com.numplates.nomera3.modules.chat.domain.usecases.messages.RefreshFirstMessageUseCase
import com.numplates.nomera3.modules.chat.domain.usecases.messages.RefreshMessageUseCase
import com.numplates.nomera3.modules.chat.domain.usecases.messages.RemoveUnreadDividerUseCase
import com.numplates.nomera3.modules.chat.domain.usecases.messages.UpdateLastUnreadMessageTsUseCase
import com.numplates.nomera3.modules.chat.domain.usecases.messages.UpdateMessageUseCase
import com.numplates.nomera3.modules.chat.domain.usecases.messages.UpdateRoomAsReadUseCase
import com.numplates.nomera3.modules.chat.domain.usecases.messages.voice.UpdateAndRefreshIsExpandedVoiceMessageUseCase
import com.numplates.nomera3.modules.chat.domain.usecases.messages.voice.UpdateIsExpandedVoiceMessageUseCase
import com.numplates.nomera3.modules.chat.domain.usecases.messages.voice.UpdateIsExpandedVoiceMessagesUseCase
import com.numplates.nomera3.modules.chat.domain.usecases.messages.voice.UpdateVoiceMessageAsStoppedUseCase
import com.numplates.nomera3.modules.chat.helpers.sendmessage.SendMessageManager
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class MessagesInteractor @Inject constructor(
    private val sendMessageManager: SendMessageManager,
    private val getMessagesUseCase: GetMessagesUseCase,
    private val updateMessageUseCase: UpdateMessageUseCase,
    private val refreshMessageUseCase: RefreshMessageUseCase,
    private val refreshFirstMessageUseCase: RefreshFirstMessageUseCase,
    private val getLastMessageUpdatedTimeUseCase: GetLastMessageUpdatedTimeUseCase,
    private val updateLastUnreadMessageTsUseCase: UpdateLastUnreadMessageTsUseCase,
    private val getMessageByIdUseCase: GetMessageByIdUseCase,
    private val observeEventMessagesUseCase: ObserveEventMessagesUseCase,
    private val observeCountMessagesUseCase: ObserveCountMessagesUseCase,
    private val readAndDecrementMessageUseCase: ReadAndDecrementMessageUseCase,
    private val observeUnreadMessageCounterUseCase: ObserveUnreadMessageCounterUseCase,
    private val removeUnreadDividerUseCase: RemoveUnreadDividerUseCase,
    private val updateRoomAsReadUseCase: UpdateRoomAsReadUseCase,
    private val getUnsentMessageCountUseCase: GetUnsentMessageCountUseCase,
    private val deleteUnsentMessageUseCase: DeleteUnsentMessageUseCase,
    private val countAllUnsentMessagesUseCase: CountAllUnsentMessagesUseCase,
    private val getNextMessagesUseCase: GetNextMessagesUseCase,
    private val updateIsExpandedVoiceMessageUseCase: UpdateIsExpandedVoiceMessageUseCase,
    private val updateIsExpandedVoiceMessagesUseCase: UpdateIsExpandedVoiceMessagesUseCase,
    private val updateAndRefreshIsExpandedVoiceMessageUseCase: UpdateAndRefreshIsExpandedVoiceMessageUseCase,
    private val updateVoiceMessageAsStoppedUseCase: UpdateVoiceMessageAsStoppedUseCase,
    private val readMessageNetworkUseCase: ReadMessageNetworkUseCase,
    private val deleteMessageNetworkUseCase: DeleteMessageNetworkUseCase,
    private val observeIncomingMessageUseCase: ObserveIncomingMessageUseCase,
    private val getResendProgressMessageUseCase: GetResendProgressMessageUseCase
) {

    suspend fun sendOnlyNetworkMessage(roomId: Long, messageText: String) {
        sendMessageManager.sendOnlyNetworkMessage(roomId, messageText)
    }

    suspend fun getMessages(
        roomId: Long,
        lastUpdatedAtMessages: Long,
        direction: MessagePaginationDirection,
    ) = getMessagesUseCase.invoke(
        roomId = roomId,
        lastUpdatedAtMessages = lastUpdatedAtMessages,
        direction = direction
    )

    suspend fun updateMessage(message: MessageEntity) = updateMessageUseCase.invoke(message)

    suspend fun refreshMessage(roomId: Long, messageId: String) = refreshMessageUseCase.invoke(roomId, messageId)

    suspend fun refreshMessage(messageId: String) = refreshMessageUseCase.invoke(messageId)

    suspend fun refreshFirstMessage(roomId: Long) = refreshFirstMessageUseCase.invoke(roomId)

    suspend fun getLastMessageUpdatedTime(roomId: Long) = getLastMessageUpdatedTimeUseCase.invoke(roomId)

    suspend fun updateLastUnreadMessageTs(
        roomId: Long,
        timestamp: Long
    ): Int {
        return updateLastUnreadMessageTsUseCase.invoke(
            roomId = roomId,
            timestamp = timestamp
        )
    }

    suspend fun getMessageById(messageId: String): MessageEntity? {
        return getMessageByIdUseCase.invoke(messageId)
    }

    fun observeEventMessages(roomId: Long): Flow<List<MessageEntity>> = observeEventMessagesUseCase.invoke(roomId)

    fun observeCountMessages(roomId: Long): Flow<Long> = observeCountMessagesUseCase.invoke(roomId)

    suspend fun readAndDecrementMessageUseCase(roomId: Long, messageId: String) =
        readAndDecrementMessageUseCase.invoke(roomId, messageId)

    fun observeUnreadMessageCounter(roomId: Long?) = observeUnreadMessageCounterUseCase.invoke(roomId)

    suspend fun removeUnreadDivider() = removeUnreadDividerUseCase.invoke()

    suspend fun updateRoomAsRead(roomId: Long) = updateRoomAsReadUseCase.invoke(roomId)

    suspend fun getUnsentMessageCount(roomId: Long) = getUnsentMessageCountUseCase.invoke(roomId)

    suspend fun deleteUnsentMessage(message: MessageEntity) = deleteUnsentMessageUseCase.invoke(message)

    suspend fun countAllUnsentMessages(roomId: Long) = countAllUnsentMessagesUseCase.invoke(roomId)

    suspend fun getNextMessages(roomId: Long, createdAt: Long) =
        getNextMessagesUseCase.invoke(roomId, createdAt)

    /**
     * По информации от BA от 15.06.2023 мы не должны хранить состояние
     * скрыт/раскрыт распознанный текст в голосовом сообщении чата при выходе и повторном заходе в чат
     * (https://nomeraworkspace.slack.com/archives/C012KLM2RSN/p1686816294045569)
     * !!! Код пока оставлю на всякий, но вызова этого метода нет
     */
    @Deprecated("Not used")
    suspend fun updateIsExpandedVoiceMessage(messageId: String, isExpanded: Boolean) =
        updateIsExpandedVoiceMessageUseCase.invoke(messageId, isExpanded)

    suspend fun updateIsExpandedVoiceMessages(roomId: Long?, isExpanded: Boolean) =
        updateIsExpandedVoiceMessagesUseCase.invoke(roomId, isExpanded)

    suspend fun updateAndRefreshIsExpandedVoiceMessage(messageId: String?, isExpanded: Boolean?): Int =
        updateAndRefreshIsExpandedVoiceMessageUseCase.invoke(messageId, isExpanded)

    suspend fun updateVoiceMessageAsStopped(roomId: Long): Int =
        updateVoiceMessageAsStoppedUseCase.invoke(roomId)

    suspend fun readMessageNetwork(roomId: Long, messageIds: List<String>): Boolean =
        readMessageNetworkUseCase.invoke(roomId, messageIds)

    suspend fun deleteMessageNetwork(
        roomId: Long,
        messageId: String,
        isBoth: Boolean
    ) = deleteMessageNetworkUseCase.invoke(roomId, messageId, isBoth)

    fun observeIncomingMessage(): Flow<MessageEntity> = observeIncomingMessageUseCase.invoke()

    suspend fun getResendProgressMessage(roomId: Long) = getResendProgressMessageUseCase.invoke(roomId)

}
