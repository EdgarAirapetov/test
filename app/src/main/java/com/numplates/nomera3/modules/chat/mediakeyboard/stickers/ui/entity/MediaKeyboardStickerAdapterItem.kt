package com.numplates.nomera3.modules.chat.mediakeyboard.stickers.ui.entity

import com.numplates.nomera3.modules.chat.mediakeyboard.favorites.ui.entity.MediakeyboardFavoriteRecentUiModel

sealed class MediaKeyboardStickerAdapterItem {
    data class StickerItem(
        val sticker: MediaKeyboardStickerUiModel,
        val packId: Int
    ) : MediaKeyboardStickerAdapterItem()

    data class StickerPackHeaderItem(
        val stickerPack: MediaKeyboardStickerPackUiModel
    ) : MediaKeyboardStickerAdapterItem()

    data class RecentStickerItem(
        val sticker: MediakeyboardFavoriteRecentUiModel
    ) : MediaKeyboardStickerAdapterItem()

    data class WidgetsItem(val widgets: List<MediaKeyboardWidget>) : MediaKeyboardStickerAdapterItem()

    data object RecentStickersHeaderItem : MediaKeyboardStickerAdapterItem()
}
