package com.numplates.nomera3.modules.userprofile.ui

data class MomentsMiniPreviewModel(
    val isMe: Boolean, val momentsPreviews: List<MomentPreviewItem>
)


data class MomentPreviewItem(val url: String, val viewed: Boolean)
