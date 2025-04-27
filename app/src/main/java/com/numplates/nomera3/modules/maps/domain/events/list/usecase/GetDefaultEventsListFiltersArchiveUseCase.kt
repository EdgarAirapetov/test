package com.numplates.nomera3.modules.maps.domain.events.list.usecase

import com.numplates.nomera3.modules.maps.domain.events.list.model.EventParticipationCategory
import com.numplates.nomera3.modules.maps.domain.events.list.model.EventsListFiltersModel
import javax.inject.Inject

class GetDefaultEventsListFiltersArchiveUseCase @Inject constructor() {
    fun invoke() = EventsListFiltersModel.EventsListFiltersArchiveModel(
        eventTypeFilter = emptyList(),
        participationCategory = EventParticipationCategory.PARTICIPANT
    )
}
