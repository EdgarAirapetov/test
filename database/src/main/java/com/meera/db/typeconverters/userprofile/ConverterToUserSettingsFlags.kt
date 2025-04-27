package com.meera.db.typeconverters.userprofile

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.meera.db.models.userprofile.UserSettingsFlags

class ConverterToUserSettingsFlags {
    companion object {

        @TypeConverter
        @JvmStatic
        fun fromUserSettingsFlags(userSettings: UserSettingsFlags?) : String? {
            if (userSettings == null) {
                return null
            }
            return Gson().toJson(userSettings)
        }

        @TypeConverter
        @JvmStatic
        fun toUserSettingsFlags(userSettingsJson: String?) : UserSettingsFlags? {
            if (userSettingsJson == null) {
                return null
            }
            val userSettingsType = object : TypeToken<UserSettingsFlags>() {}.type
            return Gson().fromJson(userSettingsJson, userSettingsType)
        }
    }
}
