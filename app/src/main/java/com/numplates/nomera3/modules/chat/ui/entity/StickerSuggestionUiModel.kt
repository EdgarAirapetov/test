package com.numplates.nomera3.modules.chat.ui.entity

import com.numplates.nomera3.modules.chat.mediakeyboard.stickers.ui.entity.MediaKeyboardStickerUiModel

data class StickerSuggestionUiModel(
    val sticker: MediaKeyboardStickerUiModel,
    var isFirst: Boolean = false,
    var isLast: Boolean = false
)
