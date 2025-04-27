package com.numplates.nomera3.modules.chat.domain.usecases

import com.meera.db.models.dialog.LastMessage
import com.meera.db.models.message.MessageEntity
import com.numplates.nomera3.modules.chat.data.repository.ChatMessageRepositoryImpl
import javax.inject.Inject

class UpdateLastMessageUseCase @Inject constructor(
    private val repository: ChatMessageRepositoryImpl
) {

    fun invoke(
        message: MessageEntity,
        lastMessage: LastMessage
    ) {
        return repository.updateLastMessage(
            message = message,
            lastMessage = lastMessage
        )
    }

}
