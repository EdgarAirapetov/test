package com.numplates.nomera3.modules.maps.domain.events.list.usecase

import com.numplates.nomera3.modules.maps.domain.events.list.model.EventParticipationCategory
import com.numplates.nomera3.modules.maps.domain.events.list.model.EventsListFiltersModel
import com.numplates.nomera3.modules.maps.domain.events.model.FilterEventDate
import javax.inject.Inject

class GetDefaultEventsListFiltersMyUseCase @Inject constructor() {
    fun invoke() = EventsListFiltersModel.EventsListFiltersMyModel(
        eventTypeFilter = emptyList(),
        eventDateFilter = FilterEventDate.ALL,
        participationCategory = EventParticipationCategory.PARTICIPANT
    )
}
