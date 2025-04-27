package com.numplates.nomera3.modules.chat.mediakeyboard.recents.ui.entity

sealed class MediaKeyboardRecentsEvent {
    object OnLoadingRecentError : MediaKeyboardRecentsEvent()
}
