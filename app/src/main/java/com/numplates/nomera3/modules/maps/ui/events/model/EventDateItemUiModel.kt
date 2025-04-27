package com.numplates.nomera3.modules.maps.ui.events.model

import java.time.LocalDate

data class EventDateItemUiModel(
    val date: LocalDate,
    val dayOfWeek: String,
    val dateString: String,
    val selected: Boolean,
)
