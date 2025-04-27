package com.numplates.nomera3.modules.maps.data.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.numplates.nomera3.modules.baseCore.data.model.CoordinatesDto
import kotlinx.parcelize.Parcelize

@Parcelize
data class EventAddressDto(
    @SerializedName("address") val addressString: String,
    @SerializedName("location") val location: CoordinatesDto,
    @SerializedName("name") val name: String,
    @SerializedName("time_zone") val timeZone: String,
    @SerializedName("distance") val distanceMeters: Float?,
) : Parcelable
