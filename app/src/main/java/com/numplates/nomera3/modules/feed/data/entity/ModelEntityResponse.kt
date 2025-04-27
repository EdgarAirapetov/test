package com.numplates.nomera3.modules.feed.data.entity

import com.google.gson.annotations.SerializedName

data class ModelEntityResponse(
        @SerializedName("brand_id")
        val brandId: Int,

        @SerializedName("model_id")
        val modelId: Int,

        @SerializedName("name")
        val name: String
)
