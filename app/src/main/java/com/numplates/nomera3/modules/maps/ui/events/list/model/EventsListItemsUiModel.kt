package com.numplates.nomera3.modules.maps.ui.events.list.model

data class EventsListItemsUiModel(
    val items: List<EventsListItem>,
    val isLoadingNextPage: Boolean,
    val isLastPage: Boolean
)
