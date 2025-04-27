package com.numplates.nomera3.modules.chat.mediakeyboard.favorites.domain.entity


data class MediakeyboardFavoriteRecentModel(
    val id: Int,
    val type: String,
    val url: String,
    val preview: String,
    val duration: Int?,
    val ratio: Float?,
    val lottieUrl: String?,
    val webpUrl: String?,
    val emoji: String?,
    val favoriteId: Long?,
    val stickerId: Int?,
    val messageId: String?,
    val gifId: String?,
    val stickerPackTitle: String?,
    val isFromMoments: Boolean
)
