package com.numplates.nomera3.data.network

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class VehicleResponse(
    @SerializedName("vehicle")
    val vehicle: Vehicle? = null
): Parcelable