package com.meera.db.typeconverters.userprofile

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.meera.db.models.userprofile.City


class ConvertToCity {
    companion object {

        @TypeConverter
        @JvmStatic
        fun fromCity(city: City?) : String? {
            if (city == null) {
                return null
            }
            return Gson().toJson(city)
        }

        @TypeConverter
        @JvmStatic
        fun toCity(cityJson: String?) : City? {
            if (cityJson == null) {
                return null
            }
            val cityType = object : TypeToken<City>() {}.type
            return Gson().fromJson(cityJson, cityType)
        }
    }

}
