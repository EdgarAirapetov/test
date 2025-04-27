package com.numplates.nomera3.modules.maps.ui.layers.model

sealed interface MapLayersUiEffect {
    data class ShowLayersTooltip(val tooltip: MapLayersTooltip) : MapLayersUiEffect
}
