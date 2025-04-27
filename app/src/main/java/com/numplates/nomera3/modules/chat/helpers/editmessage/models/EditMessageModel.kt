package com.numplates.nomera3.modules.chat.helpers.editmessage.models

import com.numplates.nomera3.modules.chat.messages.domain.model.MessageAttachmentModel

data class EditMessageModel(
    val messageId: String,
    val roomId: Long,
    val messageText: String?,
    val attachments: List<MessageAttachmentModel>?
)
