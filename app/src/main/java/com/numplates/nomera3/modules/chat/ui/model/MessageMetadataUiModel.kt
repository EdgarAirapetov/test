package com.numplates.nomera3.modules.chat.ui.model

data class MessageMetadataUiModel(
    val userId: Long? = null,
    val type: String? = null,
    val status: String? = null,
    val createdAt: Long? = null,
    val caller: MessageMetadataCallUiModel? = null,
    val callDuration: Int? = null
)

data class  MessageMetadataCallUiModel(
    val callerId: Long?
)
