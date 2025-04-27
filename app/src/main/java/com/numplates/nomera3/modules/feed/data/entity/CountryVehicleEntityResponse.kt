package com.numplates.nomera3.modules.feed.data.entity

import com.google.gson.annotations.SerializedName

data class CountryVehicleEntityResponse(
        @SerializedName("country_id")
        val countryId: Int,

        @SerializedName("flag")
        val flag: String,

        @SerializedName("name")
        val name: String
)
