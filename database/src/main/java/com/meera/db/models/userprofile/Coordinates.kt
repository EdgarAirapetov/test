package com.meera.db.models.userprofile

import com.google.gson.annotations.SerializedName

data class Coordinates (
    @SerializedName("latitude")
    val latitude: Double?,

    @SerializedName("longitude")
    val longitude: Double?
)
