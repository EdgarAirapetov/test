package com.numplates.nomera3.modules.maps.ui.model

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import com.numplates.nomera3.modules.feed.ui.entity.PostUIEntity

data class EventObjectUiModel(
    @DrawableRes
    val eventIconResId: Int,
    @DrawableRes
    val eventPinIconResId: Int,
    @ColorRes
    val eventColorResId: Int,
    val eventPost: PostUIEntity
)
