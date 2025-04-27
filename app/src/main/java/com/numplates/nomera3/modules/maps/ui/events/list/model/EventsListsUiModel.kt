package com.numplates.nomera3.modules.maps.ui.events.list.model

data class EventsListsUiModel(
    val eventsListsPages: List<EventsListPageUiModel>,
    val selectedPageIndex: Int
)
