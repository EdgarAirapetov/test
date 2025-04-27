package com.numplates.nomera3.modules.chat.messages.domain.mapper

import com.meera.db.models.message.MessageEntity
import com.numplates.nomera3.modules.chat.helpers.editmessage.models.EditMessageModel
import com.numplates.nomera3.modules.chat.messages.domain.model.MessageAttachmentModel
import javax.inject.Inject

class EditMessageMapper @Inject constructor() {

    fun map(
        oldMessage: MessageEntity,
        editedText: String?,
        editedMediaUriSet: Set<String>?
    ): EditMessageModel {
        return EditMessageModel(
            messageId = oldMessage.msgId,
            roomId = oldMessage.roomId,
            messageText = editedText ?: oldMessage.tagSpan?.text ?: oldMessage.content,
            attachments = editedMediaUriSet?.map { uriString ->
                val attachment = oldMessage.attachment.takeIf { it.url == uriString }
                    ?: oldMessage.attachments.find { it.url == uriString }
                MessageAttachmentModel(
                    uriPath = attachment?.url ?: uriString,
                    type = attachment?.type,
                    metadata = attachment?.metadata,
                    favoriteMediaId = attachment?.favoriteId,
                )
            }
        )
    }
}
