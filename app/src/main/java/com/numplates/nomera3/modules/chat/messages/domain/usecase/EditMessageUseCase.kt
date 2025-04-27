package com.numplates.nomera3.modules.chat.messages.domain.usecase

import com.numplates.nomera3.modules.chat.helpers.editmessage.models.EditMessageModel
import com.numplates.nomera3.modules.chat.messages.data.repository.MessagesRepository
import javax.inject.Inject

class EditMessageUseCase @Inject constructor(
    private val messagesRepository: MessagesRepository
) {

    suspend operator fun invoke(editedMessage: EditMessageModel) {
        messagesRepository.editMessage(
            messageId = editedMessage.messageId,
            roomId = editedMessage.roomId,
            messageText = editedMessage.messageText,
            attachments = editedMessage.attachments
        )
    }
}
