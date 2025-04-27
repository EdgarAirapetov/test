package com.numplates.nomera3.modules.chat.ui.model

data class MessageParentUiModel(
    val messageType: MessageType = MessageType.OTHER,
    val creatorName: String = "",
    val imagePreview: String = "",
    val videoPreview: String = "",
    val messageContent: String = "",
    val imageCount: Int = 0,
    val createdAt: Long = -1,
    val parentId: String = "",
    val sharedProfileUrl: String = "",
    val isDeletedSharedProfile: Boolean = false,
    val sharedCommunityUrl: String = "",
    val isPrivateCommunity: Boolean = false,
    val isDeletedSharedCommunity: Boolean = false,
    val isEvent: Boolean = false,
    val isMoment: Boolean = false,
    var metadata: MessageMetadataUiModel? = null,
)
