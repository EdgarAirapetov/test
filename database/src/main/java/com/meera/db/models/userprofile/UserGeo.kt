package com.meera.db.models.userprofile

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class UserGeo(

    @SerializedName("lat")
    val lat: Double? = null,

    @SerializedName("lon")
    val lon: Double? = null,
) : Parcelable
