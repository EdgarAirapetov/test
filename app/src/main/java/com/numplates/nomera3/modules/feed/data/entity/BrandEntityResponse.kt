package com.numplates.nomera3.modules.feed.data.entity

import com.google.gson.annotations.SerializedName

data class BrandEntityResponse(
        @SerializedName("brand_id")
        val brandId: String,

        @SerializedName("logo")
        val logo: String,

        @SerializedName("name")
        val name: String
)
