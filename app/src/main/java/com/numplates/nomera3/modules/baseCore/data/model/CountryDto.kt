package com.numplates.nomera3.modules.baseCore.data.model

import com.google.gson.annotations.SerializedName

data class CountryDto(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String?,
)