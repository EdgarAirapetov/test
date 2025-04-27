package com.numplates.nomera3.modules.maps.domain.events.model

data class EventFiltersModel(
    val eventTypeFilter: List<EventType>,
    val eventDateFilter: FilterEventDate
)
