package com.numplates.nomera3.data.network

import com.google.gson.annotations.SerializedName
import timber.log.Timber
import java.io.Serializable


data class VehicleType (

        @SerializedName("type_id")
        var typeId: String?,

        @SerializedName("name")
        var name: String?,

        @SerializedName("has_number")
        var hasNumber: Int,

        @SerializedName("has_brands")
        var hasMakes: Int,

        @SerializedName("has_models")
        var hasModels: Int,

        var selected : Boolean = false

) : Serializable {

    constructor() : this("", "", 0, 0, 0, false)

    fun getIcon(vehicleTypeMap: MutableMap<String, VehicleTypeEntity>): Int? {
        return vehicleTypeMap.get(typeId ?: "1")?.iconResId
    }

    fun getSelectedIcon(vehicleTypeMap: MutableMap<String, VehicleTypeEntity>): Int? {
        Timber.i(" VEHICLE_TYPE_ID: ${typeId}")
        return vehicleTypeMap.get(typeId ?: "1")?.iconSecectedResId
    }

    fun getSPlaceHolder(vehicleTypeMap: MutableMap<String, VehicleTypeEntity>): Int? {
        return vehicleTypeMap.get(typeId ?: "1")?.placeHolderId
    }
}