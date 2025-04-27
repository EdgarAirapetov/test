package com.numplates.nomera3.modules.chat.data

import com.meera.core.extensions.empty
import com.meera.db.models.dialog.LastMessage
import com.meera.db.models.message.MessageAttachment
import com.meera.db.models.message.MessageEntity


enum class DialogApproved(val key: Int) {
    NOT_DEFINED(0),
    ALLOW(1),
    FORBIDDEN(2);

    fun toInt() = this.key

    companion object {
        fun fromInt(intValue: Int) = when(intValue) {
            0 -> NOT_DEFINED
            1 -> ALLOW
            2 -> FORBIDDEN
            else -> null
        }
    }
}

fun LastMessage.toMessageEntity(): MessageEntity {
    return MessageEntity(
        msgId = this.msgId,
        content = this.content ?: String.empty(),
        type = this.type,
        attachment = this.attachment ?: MessageAttachment(),
        eventCode = this.eventCode,
        metadata = this.metadata,
        creator = this.creator,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt,
        deleted = this.deleted,
        sent = this.sent,
        delivered = this.delivered ?: false,
        readed = this.readed ?: false
    )
}
