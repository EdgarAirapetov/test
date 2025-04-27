package com.meera.db.models.userprofile

import com.google.gson.annotations.SerializedName

data class PhotoEntity(

    @SerializedName("id")
    val id: Long?,

    @SerializedName("link")
    val link: String?,

    @SerializedName("is_adult")
    val isAdult: Int?,
)
