package com.numplates.nomera3.data.network

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class GPSPosition (
        @SerializedName("gps_x") var gpsX: Double? = null,
        @SerializedName("gps_y") var gpsY: Double? = null

) : Serializable {
    constructor(v :GPSPosition) : this(v.gpsX, v.gpsY)
}