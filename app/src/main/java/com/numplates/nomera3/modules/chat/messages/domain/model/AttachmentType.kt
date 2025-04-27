package com.numplates.nomera3.modules.chat.messages.domain.model

import com.meera.db.models.message.MessageAttachment
import com.meera.db.models.message.MessageEntity

/**
 * Types of [MessageAttachment.type] which is used in [MessageEntity.attachment]
 */
enum class AttachmentType constructor(val type: String) {
    IMAGE("image"),
    GIF("gif"),
    AUDIO("audio"),
    VIDEO("video"),
    POST("post"),
    EVENT("event"),
    GIFT("gift"),
    PROFILE("profile"),
    COMMUNITY("community"),
    MOMENT("moment"),
    STICKER("sticker");
}
