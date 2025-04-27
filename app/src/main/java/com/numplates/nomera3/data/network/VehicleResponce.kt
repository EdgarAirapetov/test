package com.numplates.nomera3.data.network

import com.google.gson.annotations.SerializedName


data class VehicleResponce(
        @SerializedName("vehicle") var vehicle: Vehicle? = null
)