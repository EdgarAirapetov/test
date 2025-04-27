package com.numplates.nomera3.modules.maps.domain.events.list.usecase

import com.numplates.nomera3.modules.maps.domain.events.list.model.EventsListFiltersModel
import com.numplates.nomera3.modules.maps.domain.events.model.FilterEventDate
import javax.inject.Inject

class GetDefaultEventsListFiltersNearbyUseCase @Inject constructor() {
    fun invoke() = EventsListFiltersModel.EventsListFiltersNearbyModel(
        eventTypeFilter = emptyList(),
        eventDateFilter = FilterEventDate.ALL
    )
}
