package com.numplates.nomera3.modules.maps.ui.events.model

import com.numplates.nomera3.modules.maps.domain.events.model.EventType
import com.numplates.nomera3.modules.places.domain.model.PlaceModel
import java.time.LocalDate
import java.time.LocalTime

data class EventEditingSetupUiModel(
    val place: PlaceModel,
    val date: LocalDate,
    val time: LocalTime,
    val eventType: EventType
)
