package com.numplates.nomera3.modules.chat.domain.usecases.messages

import com.meera.db.models.message.MessageEntity
import com.numplates.nomera3.modules.chat.domain.ChatPersistDbRepository
import javax.inject.Inject

class GetNextMessagesUseCase @Inject constructor(
    private val repository: ChatPersistDbRepository
) {

    suspend fun invoke(roomId: Long, createdAt: Long): List<MessageEntity?> {
        return repository.getNextMessages(roomId, createdAt)
    }

}
