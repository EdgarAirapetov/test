package com.meera.db.typeconverters.userprofile

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.meera.db.models.userprofile.UserSimple

class ConvertListToUserSimple {

    companion object {

        @TypeConverter
        @JvmStatic
        fun fromUserSimple(userSimple: List<UserSimple>?): String? {
            if (userSimple.isNullOrEmpty()) {
                return null
            }
            return Gson().toJson(userSimple)
        }

        @TypeConverter
        @JvmStatic
        fun toUserSimple(userSimpleJson: String?): List<UserSimple>? {
            if (userSimpleJson.isNullOrEmpty()) {
                return null
            }
            val userSimpleType = object : TypeToken<List<UserSimple>>() {}.type
            return Gson().fromJson(userSimpleJson, userSimpleType)
        }
    }
}
