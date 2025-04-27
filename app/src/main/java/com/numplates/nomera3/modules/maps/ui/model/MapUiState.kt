package com.numplates.nomera3.modules.maps.ui.model

data class MapUiState(
    val mapMode: MapMode,
    val mapUiValues: MapUiValuesUiModel,
    val nonDefaultLayersSettings: Boolean
)
