package com.numplates.nomera3.modules.maps.ui.events.list.model

import com.numplates.nomera3.modules.maps.ui.model.MapUiAction

data class EventsListEmptyUiModel(
    val title: String,
    val message: String,
    val actionText: String?,
    val action: MapUiAction.EventsListUiAction?
)
