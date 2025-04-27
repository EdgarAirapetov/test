package com.numplates.nomera3.modules.maps.data.model

import com.google.gson.annotations.SerializedName

data class NearestFriendDto(
    @SerializedName("user_id")
    val id: Long,
    @SerializedName("lat")
    val lat: Double,
    @SerializedName("lon")
    val lon: Double
)
