package com.numplates.nomera3.modules.maps.domain.events.list.model

import com.numplates.nomera3.modules.maps.domain.events.model.EventType
import com.numplates.nomera3.modules.maps.domain.events.model.EventsListType
import com.numplates.nomera3.modules.maps.domain.events.model.FilterEventDate

sealed class EventsListFiltersModel(val eventsListType: EventsListType) {

    data class EventsListFiltersNearbyModel(
        val eventTypeFilter: List<EventType>,
        val eventDateFilter: FilterEventDate
    ) : EventsListFiltersModel(EventsListType.NEARBY)

    data class EventsListFiltersMyModel(
        val eventTypeFilter: List<EventType>,
        val eventDateFilter: FilterEventDate,
        val participationCategory: EventParticipationCategory
    ) : EventsListFiltersModel(EventsListType.MY)

    data class EventsListFiltersArchiveModel(
        val eventTypeFilter: List<EventType>,
        val participationCategory: EventParticipationCategory
    ) : EventsListFiltersModel(EventsListType.ARCHIVE)
}
