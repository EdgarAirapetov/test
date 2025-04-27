package com.numplates.nomera3.modules.chat.domain.usecases.messages

import com.meera.db.models.message.MessageEntity
import com.numplates.nomera3.modules.chat.domain.ChatMessageRepository
import com.numplates.nomera3.modules.chat.domain.params.MessagePaginationDirection
import javax.inject.Inject

class GetMessagesUseCase @Inject constructor(
    private val repository: ChatMessageRepository
) {

    suspend fun invoke(
        roomId: Long,
        lastUpdatedAtMessages: Long,
        direction: MessagePaginationDirection,
    ): List<MessageEntity> {
        return repository.getMessages(
            roomId = roomId,
            lastUpdatedAtMessages = lastUpdatedAtMessages,
            direction = direction
        )
    }

}
