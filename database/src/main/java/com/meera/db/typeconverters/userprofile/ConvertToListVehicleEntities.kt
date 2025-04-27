package com.meera.db.typeconverters.userprofile

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.meera.db.models.userprofile.VehicleEntity

class ConvertToListVehicleEntities {

    companion object {

        @TypeConverter
        @JvmStatic
        fun fromListVehicleEntity(listVehicleEntities: List<VehicleEntity>?): String? {
            if (listVehicleEntities == null) {
                return null
            }
            return Gson().toJson(listVehicleEntities)
        }

        @TypeConverter
        @JvmStatic
        fun toListVehicleEntity(listVehicleEntitiesJson: String?): List<VehicleEntity>? {
            if (listVehicleEntitiesJson == null) {
                return null
            }
            val listVehicleEntitiesType = object : TypeToken<List<VehicleEntity>>() {}.type
            return Gson().fromJson(listVehicleEntitiesJson, listVehicleEntitiesType)
        }
    }

}
