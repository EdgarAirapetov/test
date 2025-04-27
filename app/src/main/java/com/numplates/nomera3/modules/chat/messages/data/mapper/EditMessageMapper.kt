package com.numplates.nomera3.modules.chat.messages.data.mapper

import com.meera.db.models.message.MessageAttachment
import com.numplates.nomera3.modules.chat.messages.domain.model.MessageAttachmentModel
import javax.inject.Inject

class EditMessageMapper @Inject constructor() {

    fun mapEditMessageAttachment(attachment: MessageAttachmentModel): MessageAttachment {
        val metadataHashMap = attachment.metadata.let {
            val result = hashMapOf<String, Any>()
            it?.forEach { (key, value) -> result[key] = value }
            result
        }
        return MessageAttachment(
            url = attachment.uriPath,
            type = attachment.type ?: "",
            metadata = metadataHashMap,
            favoriteId = attachment.favoriteMediaId
        )
    }
}
