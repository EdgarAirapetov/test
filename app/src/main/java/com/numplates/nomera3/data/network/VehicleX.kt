package com.numplates.nomera3.data.network


import com.google.gson.annotations.SerializedName

/**
 * Short vehicle model response serach users Old rest api
 */
data class VehicleX(
    @SerializedName("brand")
    var brand: String,
    @SerializedName("country_id")
    var countryId: Int,
    @SerializedName("icon")
    var icon: String?,
    @SerializedName("model")
    var model: String,
    @SerializedName("number")
    var number: String,
    @SerializedName("type")
    var type: Int
)