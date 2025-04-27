package com.numplates.nomera3.modules.maps.ui.events.model

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.numplates.nomera3.modules.maps.domain.events.model.EventType

data class EventTypeItemUiModel(
    val type: EventType,
    val selected: Boolean,
    @StringRes
    val titleResId: Int,
    @DrawableRes
    val imgResId: Int,
    val selectedColorScheme: EventTypeItemColorSchemeUiModel
)
