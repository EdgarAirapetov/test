package com.numplates.nomera3.modules.chat.mediakeyboard.stickers.domain.entity

data class MediaKeyboardStickerPackModel(
    val id: Int,
    val title: String,
    val preview: String,
    val createdAt: Long,
    val viewed: Boolean,
    val isNew: Boolean,
    val stickers: List<MediaKeyboardStickerModel>,
    val useCount: Int = 0,
)
