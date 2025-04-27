package com.numplates.nomera3.modules.chat.mediakeyboard.stickers.ui.entity

data class MediaKeyboardStickerPackUiModel(
    val id: Int,
    val title: String,
    val preview: String,
    val createdAt: Long,
    val viewed: Boolean,
    val stickers: List<MediaKeyboardStickerUiModel>
)
