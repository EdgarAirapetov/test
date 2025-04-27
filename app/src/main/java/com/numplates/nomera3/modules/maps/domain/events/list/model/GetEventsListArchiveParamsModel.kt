package com.numplates.nomera3.modules.maps.domain.events.list.model

import com.numplates.nomera3.modules.maps.domain.events.model.EventType

data class GetEventsListArchiveParamsModel(
    val eventTypes: List<EventType>,
    val category: EventParticipationCategory,
    val offset: Int,
    val limit: Int
)
