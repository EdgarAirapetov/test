package com.numplates.nomera3.data.network

import com.google.gson.annotations.SerializedName

data class SetGPSRequestDto(
    @SerializedName("gps_x")
    val gpsX: Double,
    @SerializedName("gps_y")
    val gpsY: Double
)
