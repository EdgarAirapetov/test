package com.numplates.nomera3.modules.maps.data.model

import com.google.gson.annotations.SerializedName

data class MapWidgetWeatherDto(
    @SerializedName("temperature_fahrenheit") val temperatureFahrenheit: Float,
    @SerializedName("temperature_celsius") val temperatureCelsius: Float,
    @SerializedName("description") val description: String,
    @SerializedName("image_link") val animationUrl: String,
)
