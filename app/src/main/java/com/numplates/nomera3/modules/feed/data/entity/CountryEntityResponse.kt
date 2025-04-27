package com.numplates.nomera3.modules.feed.data.entity

import com.google.gson.annotations.SerializedName

data class CountryEntityResponse(
        @SerializedName("id")
        val id: Long?,

        @SerializedName("name")
        val name: String?
)