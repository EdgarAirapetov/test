package com.numplates.nomera3.modules.chat.mediakeyboard.picker.data.entity

sealed class MediaKeyboardViewEvent {
    object SendSelectedViewEvent : MediaKeyboardViewEvent()
    object CheckButtonsState : MediaKeyboardViewEvent()
}
