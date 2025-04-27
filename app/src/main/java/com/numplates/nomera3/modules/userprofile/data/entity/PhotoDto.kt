package com.numplates.nomera3.modules.userprofile.data.entity

import com.google.gson.annotations.SerializedName

data class PhotoDto(
    @SerializedName("id")
    val id: Long?,

    @SerializedName("link")
    val link: String?,

    @SerializedName("is_adult")
    val isAdult: Int?,
)
