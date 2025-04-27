package com.numplates.nomera3.modules.maps.ui.events.model

import androidx.annotation.ColorRes

data class EventTypeItemColorSchemeUiModel(
    @ColorRes
    val backgroundColorResId: Int,
    @ColorRes
    val outlineColorResId: Int,
    @ColorRes
    val textColorResId: Int
)
