package com.numplates.nomera3.modules.chat.mediakeyboard.stickers.domain.entity

import com.numplates.nomera3.modules.chat.mediakeyboard.favorites.domain.entity.MediakeyboardFavoriteRecentModel

data class MediaKeyboardStickersAndRecentStickersModel(
    val stickerPacks: List<MediaKeyboardStickerPackModel> = emptyList(),
    val recentStickers: List<MediakeyboardFavoriteRecentModel> = emptyList()
)
