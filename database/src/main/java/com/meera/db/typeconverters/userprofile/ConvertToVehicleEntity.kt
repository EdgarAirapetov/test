package com.meera.db.typeconverters.userprofile

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.meera.db.models.userprofile.VehicleEntity

class ConvertToVehicleEntity {

    companion object {

        @TypeConverter
        @JvmStatic
        fun fromVehicleEntity(vehicleEntity: VehicleEntity?): String? {
            if (vehicleEntity == null) {
                return null
            }
            return Gson().toJson(vehicleEntity)
        }

        @TypeConverter
        @JvmStatic
        fun toVehicleEntity(vehicleEntityJson: String?): VehicleEntity? {
            if (vehicleEntityJson == null) {
                return null
            }
            val vehicleEntityType = object : TypeToken<VehicleEntity>() {}.type
            return Gson().fromJson(vehicleEntityJson, vehicleEntityType)
        }
    }

}
