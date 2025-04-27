package com.numplates.nomera3.modules.maps.domain.model

import com.numplates.nomera3.modules.maps.domain.events.model.EventFiltersModel

data class MapSettingsModel(
    val mapMode: MapMode,
    val showMen: Boolean,
    val showWomen: Boolean,
    val showFriendsOnly: Boolean,
    val showPeople: Boolean,
    val showEvents: Boolean,
    val showFriends: Boolean,
    val eventFilters: EventFiltersModel
)
