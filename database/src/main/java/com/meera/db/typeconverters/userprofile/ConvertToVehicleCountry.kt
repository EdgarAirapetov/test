package com.meera.db.typeconverters.userprofile

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.meera.db.models.userprofile.VehicleCountry

class ConvertToVehicleCountry {

    companion object {

        @TypeConverter
        @JvmStatic
        fun fromVehicleCountry(vehicleCountry: VehicleCountry?): String? {
            if (vehicleCountry == null) {
                return null
            }
            return Gson().toJson(vehicleCountry)
        }

        @TypeConverter
        @JvmStatic
        fun toVehicleCountry(vehicleCountryJson: String?): VehicleCountry? {
            if (vehicleCountryJson == null) {
                return null
            }
            val vehicleCountryType = object : TypeToken<VehicleCountry>() {}.type
            return Gson().fromJson(vehicleCountryJson, vehicleCountryType)
        }
    }

}
