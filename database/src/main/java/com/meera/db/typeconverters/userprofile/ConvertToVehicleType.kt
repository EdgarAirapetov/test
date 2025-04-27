package com.meera.db.typeconverters.userprofile

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.meera.db.models.userprofile.VehicleType

class ConvertToVehicleType {

    companion object {

        @TypeConverter
        @JvmStatic
        fun fromVehicleType(vehicleType: VehicleType?): String? {
            if (vehicleType == null) {
                return null
            }
            return Gson().toJson(vehicleType)
        }

        @TypeConverter
        @JvmStatic
        fun toVehicleType(vehicleTypeJson: String?): VehicleType? {
            if (vehicleTypeJson == null) {
                return null
            }
            val vehicleTypeType = object : TypeToken<VehicleType>() {}.type
            return Gson().fromJson(vehicleTypeJson, vehicleTypeType)
        }
    }

}
