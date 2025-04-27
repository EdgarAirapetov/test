package com.meera.db.typeconverters.userprofile

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.meera.db.models.userprofile.UserSimple

class ConvertToUserSimple {

    companion object {

        @TypeConverter
        @JvmStatic
        fun fromUserSimple(userSimple: UserSimple?): String? {
            if (userSimple == null) {
                return null
            }
            return Gson().toJson(userSimple)
        }

        @TypeConverter
        @JvmStatic
        fun toUserSimple(userSimpleJson: String?): UserSimple? {
            if (userSimpleJson == null) {
                return null
            }
            val userSimpleType = object : TypeToken<UserSimple>() {}.type
            return Gson().fromJson(userSimpleJson, userSimpleType)
        }
    }

}
