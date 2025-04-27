package com.numplates.nomera3.modules.chat.domain.usecases.messages

import com.meera.db.models.message.MessageEntity
import com.numplates.nomera3.modules.chat.domain.ChatMessageRepository
import javax.inject.Inject

class GetResendProgressMessageUseCase @Inject constructor(
    private val repository: ChatMessageRepository
) {

    suspend fun invoke(roomId: Long): List<MessageEntity>? {
        return repository.getResendProgressMessages(roomId)
    }

}
