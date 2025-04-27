package com.numplates.nomera3.modules.maps.ui

import com.numplates.nomera3.modules.maps.ui.model.MapUiAction

interface MapUiActionHandler {
    fun handleOuterMapUiAction(uiAction: MapUiAction)
}
