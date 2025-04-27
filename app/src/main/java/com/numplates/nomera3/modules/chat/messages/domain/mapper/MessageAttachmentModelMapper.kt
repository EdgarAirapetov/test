package com.numplates.nomera3.modules.chat.messages.domain.mapper

import com.meera.db.models.message.MessageAttachment
import com.numplates.nomera3.modules.chat.messages.domain.model.MessageAttachmentModel
import javax.inject.Inject

class MessageAttachmentModelMapper @Inject constructor() {

    fun map(messageAttachment: MessageAttachment): MessageAttachmentModel {
        return MessageAttachmentModel(
            uriPath = messageAttachment.url,
            type = messageAttachment.type,
            metadata = messageAttachment.metadata,
            favoriteMediaId = messageAttachment.favoriteId
        )
    }
}
