package com.numplates.nomera3.modules.chat.mediakeyboard.favorites.ui.entity

data class MediakeyboardFavoriteRecentUiModel(
    val id: Int,
    val type: FavoriteRecentType,
    val url: String,
    val preview: String,
    val duration: Int? = null,
    val ratio: Float? = null,
    val lottieUrl: String? = null,
    val webpUrl: String? = null,
    val stickerId: Int? = null,
    val emoji: String? = null,
    val favoriteId: Long? = null,
    val messageId: String? = null,
    val gifId: String? = null,
    val stickerCategory: String? = null
) {

    enum class FavoriteRecentType(val value: String) {
        IMAGE("image"),
        VIDEO("video"),
        GIF("gif"),
        STICKER("sticker")
    }

}
