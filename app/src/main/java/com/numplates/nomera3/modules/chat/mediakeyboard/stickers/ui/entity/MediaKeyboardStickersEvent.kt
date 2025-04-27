package com.numplates.nomera3.modules.chat.mediakeyboard.stickers.ui.entity

sealed class MediaKeyboardStickersEvent {
    object OnLoadingStickersError : MediaKeyboardStickersEvent()
}
