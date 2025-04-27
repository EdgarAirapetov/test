package com.numplates.nomera3.modules.chat.mediakeyboard.stickers.ui.entity

import com.numplates.nomera3.modules.chat.mediakeyboard.favorites.ui.entity.MediakeyboardFavoriteRecentUiModel

data class MediaKeyboardStickersState(
    val stickerPacks: List<MediaKeyboardStickerPackUiModel> = emptyList(),
    val recentStickers: List<MediakeyboardFavoriteRecentUiModel> = emptyList(),
    val widgets: List<MediaKeyboardWidget> = emptyList()
)
