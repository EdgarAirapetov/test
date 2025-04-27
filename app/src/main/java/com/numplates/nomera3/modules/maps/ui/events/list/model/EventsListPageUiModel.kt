package com.numplates.nomera3.modules.maps.ui.events.list.model

import com.numplates.nomera3.modules.maps.domain.events.model.EventsListType

data class EventsListPageUiModel(
    val eventsListType: EventsListType,
    val title: String,
    val eventsListItems: EventsListItemsUiModel,
    val filters: EventsListFiltersUiModel,
    val emptyUiModel: EventsListEmptyUiModel
)
