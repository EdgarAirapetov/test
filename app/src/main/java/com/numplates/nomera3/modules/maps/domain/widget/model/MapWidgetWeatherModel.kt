package com.numplates.nomera3.modules.maps.domain.widget.model

import java.io.File

data class MapWidgetWeatherModel(
    val temperatureFahrenheit: Float,
    val temperatureCelsius: Float,
    val description: String,
    val animationFile: File?,
)
