package com.numplates.nomera3.modules.gifservice.ui.entity

import com.numplates.nomera3.modules.chat.mediakeyboard.stickers.ui.entity.MediaKeyboardStickerPackUiModel

sealed class GifMenuCallbackEvents {

    object OnHideKeyboard: GifMenuCallbackEvents()

    data class OnStickerPackViewed(val stickerPack: MediaKeyboardStickerPackUiModel) : GifMenuCallbackEvents()

}
