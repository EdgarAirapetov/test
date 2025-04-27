package com.numplates.nomera3.modules.baseCore.data.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class CoordinatesDto(
    @SerializedName("latitude") val lat: Double,
    @SerializedName("longitude") val lon: Double,
) : Parcelable
