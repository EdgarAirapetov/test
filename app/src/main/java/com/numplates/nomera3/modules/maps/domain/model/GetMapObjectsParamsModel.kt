package com.numplates.nomera3.modules.maps.domain.model

data class GetMapObjectsParamsModel(
    val gpsXMin: Double,
    val gpsXMax: Double,
    val gpsYMin: Double,
    val gpsYMax: Double,
    val zoom: Double
)
