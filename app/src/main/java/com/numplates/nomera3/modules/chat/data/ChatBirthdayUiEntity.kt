package com.numplates.nomera3.modules.chat.data

import androidx.annotation.ColorRes

data class ChatBirthdayUiEntity(
    @Deprecated("we don't need this flag in meera classes as we have birthdayTextRanges")
    val isSomeOneHasBirthday: Boolean = false,
    val birthdayTextRanges: List<IntRange>? = null,
    @ColorRes val birthdayTextColor: Int
)
