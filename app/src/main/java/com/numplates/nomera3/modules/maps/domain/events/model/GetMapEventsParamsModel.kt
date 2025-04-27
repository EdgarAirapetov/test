package com.numplates.nomera3.modules.maps.domain.events.model

import com.numplates.nomera3.modules.maps.domain.model.MapBoundsModel

data class GetMapEventsParamsModel(
    val bounds: MapBoundsModel,
    val eventTypes: List<EventType>,
    val timeFilter: FilterEventDate
)
