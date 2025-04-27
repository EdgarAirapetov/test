package com.numplates.nomera3.modules.maps.ui.events.model

import java.time.LocalDate
import java.time.LocalTime
import java.util.TimeZone

data class EventTimeUiModel(
    val minimumTime: LocalTime,
    val minimumDate: LocalDate,
    val time: LocalTime,
    val date: LocalDate,
    val timeZone: TimeZone
)
