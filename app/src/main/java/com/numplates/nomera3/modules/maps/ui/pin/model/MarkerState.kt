package com.numplates.nomera3.modules.maps.ui.pin.model

sealed interface MarkerState {
    object UpToDate : MarkerState
    object NeedToCreate : MarkerState
    object NeedToUpdate : MarkerState
}
