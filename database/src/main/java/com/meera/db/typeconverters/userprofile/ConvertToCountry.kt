package com.meera.db.typeconverters.userprofile

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.meera.db.models.userprofile.Country

class ConvertToCountry {
    companion object {

        @TypeConverter
        @JvmStatic
        fun fromCountry(country: Country?) : String? {
            if (country == null) {
                return null
            }
            return Gson().toJson(country)
        }

        @TypeConverter
        @JvmStatic
        fun toCity(countryJson: String?) : Country? {
            if (countryJson == null) {
                return null
            }
            val countryType = object : TypeToken<Country>() {}.type
            return Gson().fromJson(countryJson, countryType)
        }
    }

}
