package com.numplates.nomera3.modules.maps.domain.widget.model

import java.util.TimeZone

data class MapWidgetPointInfoModel(
    val place: MapWidgetPlaceModel,
    val timeZone: TimeZone,
    val weather: MapWidgetWeatherModel?
)
