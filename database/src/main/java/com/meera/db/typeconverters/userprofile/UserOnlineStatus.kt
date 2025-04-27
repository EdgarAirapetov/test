package com.meera.db.typeconverters.userprofile

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.meera.db.models.userprofile.UserOnlineStatus

class ConverterToUserOnlineStatus {

    companion object {

        @TypeConverter
        @JvmStatic
        fun fromUserOnlineStatus(userOnline: UserOnlineStatus?): String? {
            if (userOnline == null) {
                return null
            }
            return Gson().toJson(userOnline)
        }

        @TypeConverter
        @JvmStatic
        fun toUserOnlineStatus(userOnlineJson: String?): UserOnlineStatus? {
            if (userOnlineJson == null) {
                return null
            }
            val userOnlineType = object : TypeToken<UserOnlineStatus>() {}.type
            return Gson().fromJson(userOnlineJson, userOnlineType)
        }
    }

}
