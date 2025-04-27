package com.numplates.nomera3.modules.registration.data.entity

import com.google.gson.annotations.SerializedName

data class RegistrationCountryDto(
    @SerializedName("name") val name: String,
    @SerializedName("code") val code: String,
    @SerializedName("mask") val mask: String,
    @SerializedName("flag") val flag: String
)
