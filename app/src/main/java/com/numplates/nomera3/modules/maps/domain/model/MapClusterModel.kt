package com.numplates.nomera3.modules.maps.domain.model

data class MapClusterModel(
    val id: Long,
    val size: String,
    val gpsX: Double,
    val gpsY: Double,
    val capacity: String,
    val users: List<MapUserModel>
)
