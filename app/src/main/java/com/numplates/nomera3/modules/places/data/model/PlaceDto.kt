package com.numplates.nomera3.modules.places.data.model

import com.google.gson.annotations.SerializedName
import com.numplates.nomera3.modules.baseCore.data.model.CoordinatesDto

data class PlaceDto(
    @SerializedName("address")
    val addressString: String,
    @SerializedName("location")
    val location: CoordinatesDto,
    @SerializedName("name")
    val name: String,
    @SerializedName("time_zone")
    val timeZone: String,
    @SerializedName("place_id")
    val placeId: Long,
)
