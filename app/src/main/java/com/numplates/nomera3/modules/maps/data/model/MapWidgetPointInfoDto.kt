package com.numplates.nomera3.modules.maps.data.model

import com.google.gson.annotations.SerializedName

data class MapWidgetPointInfoDto(
    @SerializedName("place") val place: MapWidgetPlaceDto,
    @SerializedName("timezone") val timeZone: String,
    @SerializedName("weather") val weather: MapWidgetWeatherDto?
)
