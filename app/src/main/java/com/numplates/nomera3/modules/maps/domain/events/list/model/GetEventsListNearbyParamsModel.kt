package com.numplates.nomera3.modules.maps.domain.events.list.model

import com.numplates.nomera3.modules.maps.domain.events.model.EventType
import com.numplates.nomera3.modules.maps.domain.events.model.FilterEventDate

data class GetEventsListNearbyParamsModel(
    val eventTypes: List<EventType>,
    val timeFilter: FilterEventDate,
    val offset: Int,
    val limit: Int,
)
