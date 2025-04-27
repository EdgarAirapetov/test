package com.numplates.nomera3.modules.chat.mediakeyboard.stickers.ui.entity

sealed class MediaKeyboardStickerUiAction {

    data class ClearRecentStickers(val isForMoment: Boolean) : MediaKeyboardStickerUiAction()

    data class DeleteRecentSticker(
        val recentId: Int,
        val stickerId: Int?,
        val isForMoment: Boolean
    ) : MediaKeyboardStickerUiAction()

}
