package com.meera.db.typeconverters.dialog

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.meera.db.models.dialog.UserChat

class ConvertToUserChat {

    companion object {

        @TypeConverter
        @JvmStatic
        fun fromUserChat(user: UserChat?): String? {
            if (user == null) {
                return null
            }
            return Gson().toJson(user)
        }

        @TypeConverter
        @JvmStatic
        fun toUserChat(userJson: String?): UserChat? {
            if (userJson == null) {
                return null
            }
            val userType = object : TypeToken<UserChat>() {}.type
            return Gson().fromJson(userJson, userType)
        }
    }

}
