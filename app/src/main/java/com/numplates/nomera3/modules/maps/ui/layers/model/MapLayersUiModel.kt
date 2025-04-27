package com.numplates.nomera3.modules.maps.ui.layers.model

import com.meera.uikit.widgets.userpic.UserpicUiModel
import com.numplates.nomera3.modules.maps.ui.events.list.filters.model.EventFilterDateUiModel
import com.numplates.nomera3.modules.maps.ui.events.list.filters.model.EventFilterTypeUiModel

data class MapLayersUiModel(
    val showPeople: Boolean,
    val showEvents: Boolean,
    val showFriends: Boolean,
    val eventFilterType: EventFilterTypeUiModel,
    val eventFilterDate: EventFilterDateUiModel,
    val selectedUserVisibilityOnMapTypeIndex: Int,
    val showEnableLocationStub: Boolean,
    val userpicConfig: UserpicUiModel,
    val showNonDefaultEventSettings: Boolean,
    val isEventsEnabled: Boolean
)
