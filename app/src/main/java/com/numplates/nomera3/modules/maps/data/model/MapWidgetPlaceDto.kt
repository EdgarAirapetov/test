package com.numplates.nomera3.modules.maps.data.model

import com.google.gson.annotations.SerializedName

data class MapWidgetPlaceDto(
    @SerializedName("country") val country: String?,
    @SerializedName("state") val state: String?,
    @SerializedName("county") val county: String?,
    @SerializedName("district") val district: String?,
    @SerializedName("city") val city: String?,
    @SerializedName("street") val street: String?,
    @SerializedName("house") val house: String?,
)
