package com.numplates.nomera3.modules.feed.data.entity

import com.google.gson.annotations.SerializedName

data class VehicleTypeEntityResponse(
        @SerializedName("has_brands")
        val hasBrands: Int,

        @SerializedName("has_models")
        val hasModels: Int,

        @SerializedName("has_number")
        val hasNumber: Int,

        @SerializedName("name")
        val name: String,

        @SerializedName("type_id")
        val typeId: Int
)
