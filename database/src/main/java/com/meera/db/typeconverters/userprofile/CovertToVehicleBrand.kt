package com.meera.db.typeconverters.userprofile

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.meera.db.models.userprofile.VehicleBrand

class CovertToVehicleBrand {

    companion object {

        @TypeConverter
        @JvmStatic
        fun fromVehicleBrand(vehicleBrand: VehicleBrand?): String? {
            if (vehicleBrand == null) {
                return null
            }
            return Gson().toJson(vehicleBrand)
        }

        @TypeConverter
        @JvmStatic
        fun toVehicleBrand(vehicleBrandJson: String?): VehicleBrand? {
            if (vehicleBrandJson == null) {
                return null
            }
            val vehicleBrandType = object : TypeToken<VehicleBrand>() {}.type
            return Gson().fromJson(vehicleBrandJson, vehicleBrandType)
        }
    }

}
