package com.numplates.nomera3.modules.chat.mediakeyboard.stickers.ui.entity

data class MediaKeyboardStickerUiModel(
    val id: Int,
    val title: String,
    val url: String,
    val lottieUrl: String?,
    val webpUrl: String?,
    val emoji: List<String>,
    val keywords: List<String>,
    val stickerPackId: Int,
    val stickerPackTitle: String
)
