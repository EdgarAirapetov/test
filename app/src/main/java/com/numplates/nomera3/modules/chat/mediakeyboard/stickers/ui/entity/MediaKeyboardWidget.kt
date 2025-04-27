package com.numplates.nomera3.modules.chat.mediakeyboard.stickers.ui.entity

import androidx.annotation.StringRes
import com.numplates.nomera3.R

enum class MediaKeyboardWidget(
    @StringRes val titleRes: Int
) {
    TIME_WIDGET(R.string.widget_time),
    MUSIC_WIDGET(R.string.widget_music)
}
