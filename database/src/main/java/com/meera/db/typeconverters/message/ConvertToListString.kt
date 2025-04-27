package com.meera.db.typeconverters.message

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class ConvertToListString {

    companion object {

        @TypeConverter
        @JvmStatic
        fun fromListString(message: List<String>?): String? {
            if (message == null) {
                return null
            }
            return Gson().toJson(message)
        }

        @TypeConverter
        @JvmStatic
        fun toListString(listStingJson: String?): List<String>? {
            val messageType = object : TypeToken<List<String>>() {}.type
            return Gson().fromJson(listStingJson, messageType)
        }
    }

}

