package com.meera.db.typeconverters.message

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class ConvertToHashMap {

    companion object {

        @TypeConverter
        @JvmStatic
        fun fromHashMap(map: HashMap<String, Any>): String {
            return Gson().toJson(map)
        }

        @TypeConverter
        @JvmStatic
        fun toHashMap(mapJson: String): HashMap<String, Any> {
            val creatorType = object : TypeToken<HashMap<String, Any>>() {}.type
            return Gson().fromJson(mapJson, creatorType)
        }
    }

}

