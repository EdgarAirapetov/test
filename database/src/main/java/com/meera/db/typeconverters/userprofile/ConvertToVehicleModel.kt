package com.meera.db.typeconverters.userprofile

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.meera.db.models.userprofile.VehicleModel

class ConvertToVehicleModel {

    companion object {

        @TypeConverter
        @JvmStatic
        fun fromVehicleModel(vehicleModel: VehicleModel?): String? {
            if (vehicleModel == null) {
                return null
            }
            return Gson().toJson(vehicleModel)
        }

        @TypeConverter
        @JvmStatic
        fun toVehicleModel(vehicleModelJson: String?): VehicleModel? {
            if (vehicleModelJson == null) {
                return null
            }
            val vehicleModelType = object : TypeToken<VehicleModel>() {}.type
            return Gson().fromJson(vehicleModelJson, vehicleModelType)
        }
    }

}
