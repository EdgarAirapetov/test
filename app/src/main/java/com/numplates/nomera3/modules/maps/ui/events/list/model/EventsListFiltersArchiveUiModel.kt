package com.numplates.nomera3.modules.maps.ui.events.list.model

import com.numplates.nomera3.modules.maps.domain.events.list.model.EventParticipationCategory
import com.numplates.nomera3.modules.maps.ui.events.list.filters.model.EventFilterTypeUiModel

data class EventsListFiltersArchiveUiModel(
    val eventFilterType: EventFilterTypeUiModel,
    val eventParticipationCategory: EventParticipationCategory
)
