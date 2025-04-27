package com.numplates.nomera3.modules.chat.mediakeyboard.data.entity

import com.meera.core.extensions.empty

data class TemporaryMessageText(
    val roomId: Long = 0,
    val text: String = String.empty(),
    val name: String = String.empty(),
)
