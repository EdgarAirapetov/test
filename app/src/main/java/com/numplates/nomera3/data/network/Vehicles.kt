package com.numplates.nomera3.data.network

import com.google.gson.annotations.SerializedName
import com.numplates.nomera3.data.network.core.ListResponse


data class Vehicles(
        @SerializedName("vehicles") var vehicles: List<Vehicle?>? = null
) : ListResponse<Vehicle?>() {
    override fun getList(): List<Vehicle?>? {
        return vehicles
    }
}