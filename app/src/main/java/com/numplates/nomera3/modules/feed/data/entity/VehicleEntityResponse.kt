package com.numplates.nomera3.modules.feed.data.entity

import com.google.gson.annotations.SerializedName

data class VehicleEntityResponse(
        @SerializedName("brand")
        val brand: BrandEntityResponse,

        @SerializedName("country")
        val country: CountryVehicleEntityResponse,

        @SerializedName("description")
        val description: String,

        @SerializedName("image")
        val image: String,

        @SerializedName("is_main")
        val itsMain: Int,

        @SerializedName("model")
        val model: ModelEntityResponse,

        @SerializedName("number")
        val number: String,

        @SerializedName("type")
        val type: VehicleTypeEntityResponse,

        @SerializedName("vehicle_id")
        val vehicleId: Int
)
