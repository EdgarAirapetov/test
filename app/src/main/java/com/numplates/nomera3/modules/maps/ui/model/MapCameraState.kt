package com.numplates.nomera3.modules.maps.ui.model

sealed interface MapCameraState {
    object Idle : MapCameraState
    data class Moving(val initiatedByUser: Boolean) : MapCameraState
}
