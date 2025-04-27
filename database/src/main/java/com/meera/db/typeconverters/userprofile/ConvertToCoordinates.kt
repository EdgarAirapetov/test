package com.meera.db.typeconverters.userprofile

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.meera.db.models.userprofile.Coordinates



class ConvertToCoordinates {
    companion object {

        @TypeConverter
        @JvmStatic
        fun fromCoordinates(coordinates: Coordinates?): String? {
            if (coordinates == null) {
                return null
            }
            return Gson().toJson(coordinates)
        }

        @TypeConverter
        @JvmStatic
        fun toCoordinates(coordinatesJson: String?): Coordinates? {
            if (coordinatesJson == null) {
                return null
            }
            val coordinatesType = object : TypeToken<Coordinates>() {}.type
            return Gson().fromJson(coordinatesJson, coordinatesType)
        }
    }
}
