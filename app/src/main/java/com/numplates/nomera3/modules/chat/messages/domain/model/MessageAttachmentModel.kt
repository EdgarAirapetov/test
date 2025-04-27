package com.numplates.nomera3.modules.chat.messages.domain.model

data class MessageAttachmentModel(
    val uriPath: String,
    val type: String? = null,
    val metadata: Map<String, Any>? = null,
    val favoriteMediaId: Int? = null //mediakeyboard favorite id
)
