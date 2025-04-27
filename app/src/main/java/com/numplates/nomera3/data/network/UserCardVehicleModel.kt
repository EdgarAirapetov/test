package com.numplates.nomera3.data.network

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class UserCardVehicleModel(
        @SerializedName("country_id") var countryId: Int,
        @SerializedName("type") var type: Int,
        @SerializedName("brand") var brand: String,
        @SerializedName("model") var model: String,
        @SerializedName("icon") var icon: String,
        @SerializedName("number") var number: String
) : Serializable