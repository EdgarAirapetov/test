package com.numplates.nomera3.data.network

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class MediaPositioningDto(
    @SerializedName("x")
    val x: Double? = 0.0,
    @SerializedName("y")
    val y: Double? = 0.0
) : Serializable
