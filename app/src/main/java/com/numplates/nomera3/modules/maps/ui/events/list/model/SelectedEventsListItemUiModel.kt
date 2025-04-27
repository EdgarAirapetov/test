package com.numplates.nomera3.modules.maps.ui.events.list.model

import com.numplates.nomera3.modules.maps.domain.events.model.EventsListType

data class SelectedEventsListItemUiModel(
    val listType: EventsListType,
    val itemPosition: Int,
    val eventItem: EventsListItem
)
