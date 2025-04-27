package com.numplates.nomera3.modules.maps.ui.events.model

import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import com.numplates.nomera3.modules.maps.domain.events.model.EventStatus

data class EventStatusUiModel(
    val status: EventStatus,
    @StringRes
    val statusTextResId: Int,
    @ColorRes
    val textColorResId: Int,
)
