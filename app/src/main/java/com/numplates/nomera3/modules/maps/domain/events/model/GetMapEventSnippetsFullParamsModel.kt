package com.numplates.nomera3.modules.maps.domain.events.model

import com.numplates.nomera3.modules.baseCore.domain.model.CoordinatesModel

data class GetMapEventSnippetsFullParamsModel(
    val selectedEventId: Long,
    val excludedEventIds: List<Long>,
    val location: CoordinatesModel,
    val eventTypes: List<EventType>,
    val timeFilter: FilterEventDate,
    val limit: Int
)
