package com.numplates.nomera3.modules.maps.ui.pin.model

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes

data class EventLargePinUiModel(
    val id: Long,
    val title: String,
    val image: EventPinImage,
    @DrawableRes
    val eventIconResId: Int,
    @ColorRes
    val eventColorResId: Int
)
