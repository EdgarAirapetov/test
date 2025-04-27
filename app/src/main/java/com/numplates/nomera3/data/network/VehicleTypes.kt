package com.numplates.nomera3.data.network

import com.google.gson.annotations.SerializedName
import com.numplates.nomera3.data.network.core.ListResponse


data class VehicleTypes(
        @SerializedName("vehicle_types") var vehicleTypes: List<VehicleType?>? = null
) : ListResponse<VehicleType?>() {
    override fun getList(): List<VehicleType?>? {
        return vehicleTypes
    }
}