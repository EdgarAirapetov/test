package com.numplates.nomera3.modules.chat.ui.model

import com.numplates.nomera3.R

sealed class ChatBackgroundType(val background: Any?) {
    data object None : ChatBackgroundType(null)
    data object Birthday : ChatBackgroundType(R.drawable.meera_img_birthday_chat_background)
    class Holiday(background: String?) : ChatBackgroundType(background)
    class RoomStyle(background: String?) : ChatBackgroundType(background)
}
