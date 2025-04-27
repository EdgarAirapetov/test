package com.numplates.nomera3.modules.chat

import android.net.Uri
import com.numplates.nomera3.modules.baseCore.helper.amplitude.chat.mediakeyboard.AmplitudeMediaKeyboardWhereProperty
import com.numplates.nomera3.modules.chat.mediakeyboard.favorites.ui.entity.MediakeyboardFavoriteRecentUiModel
import com.numplates.nomera3.modules.chat.mediakeyboard.stickers.ui.entity.MediaKeyboardStickerPackUiModel
import com.numplates.nomera3.modules.chat.mediakeyboard.stickers.ui.entity.MediaKeyboardStickerUiModel
import com.numplates.nomera3.modules.chat.mediakeyboard.stickers.ui.entity.MediaKeyboardWidget
import com.numplates.nomera3.modules.chat.mediakeyboard.ui.entity.MediaPreviewType
import com.numplates.nomera3.modules.gifservice.ui.entity.GiphyEntity

interface MediaKeyboardCallback {
    fun onGifClicked(
        gifUri: Uri,
        aspect: Double,
        giphyEntity: GiphyEntity?,
        gifSentWhereProp: AmplitudeMediaKeyboardWhereProperty
    ) = Unit
    fun onGifLongClicked(id: String, preview: String, url: String, ratio: Double)
    fun onSearchFieldClicked() = Unit
    fun onFavoriteRecentClicked(
        favoriteRecent: MediakeyboardFavoriteRecentUiModel,
        type: MediaPreviewType,
        deleteRecentListener: (Int) -> Unit = {}
    )

    fun onFavoriteRecentLongClicked(
        favoriteRecent: MediakeyboardFavoriteRecentUiModel,
        type: MediaPreviewType,
        deleteClickListener: (Int) -> Unit = {}
    )

    fun onScrollToNewStickerPack(stickerPack: MediaKeyboardStickerPackUiModel)
    fun onScrollToRecentStickers()
    fun onScrollToWidgets() = Unit
    fun onStickerClicked(sticker: MediaKeyboardStickerUiModel, emoji: String?)
    fun onStickerLongClicked(sticker: MediaKeyboardStickerUiModel)
    fun onWidgetClicked(widget: MediaKeyboardWidget) = Unit
}
