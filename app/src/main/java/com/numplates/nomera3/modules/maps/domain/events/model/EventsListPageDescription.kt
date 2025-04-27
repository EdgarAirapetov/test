package com.numplates.nomera3.modules.maps.domain.events.model

import com.numplates.nomera3.modules.maps.ui.events.list.model.EventsListPageFiltersConfigUiModel

data class EventsListPageDescription(
    val type: EventsListType,
    val title: String,
    val filtersConfig: EventsListPageFiltersConfigUiModel
)
