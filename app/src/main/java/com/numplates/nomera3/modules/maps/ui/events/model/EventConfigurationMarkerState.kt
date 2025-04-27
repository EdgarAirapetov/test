package com.numplates.nomera3.modules.maps.ui.events.model

sealed class EventConfigurationMarkerState(val isLevitating: Boolean) {
    object Default : EventConfigurationMarkerState(false)
    data class Progress(val isDragging: Boolean) : EventConfigurationMarkerState(isDragging)
    object Error : EventConfigurationMarkerState(false)
    data class Address(val markerAddress: String) : EventConfigurationMarkerState(false)
}
