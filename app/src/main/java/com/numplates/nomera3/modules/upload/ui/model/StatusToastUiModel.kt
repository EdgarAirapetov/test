package com.numplates.nomera3.modules.upload.ui.model

data class StatusToastUiModel(
    val state: StatusToastState,
    val imageUrl: String?,
    val canPlayContent: Boolean,
    val action: StatusToastActionUiModel?
)
