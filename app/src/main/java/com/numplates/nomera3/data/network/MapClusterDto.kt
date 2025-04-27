package com.numplates.nomera3.data.network

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class MapClusterDto(
        @SerializedName("size") val size: String,
        @SerializedName("gps_x") val gpsX: Double,
        @SerializedName("gps_y") val gpsY: Double,
        @SerializedName("id") val clusterId: Long,
        @SerializedName("capacity") val capacity: String,
        @SerializedName("users") val users: List<MapUserDto>
): Serializable
