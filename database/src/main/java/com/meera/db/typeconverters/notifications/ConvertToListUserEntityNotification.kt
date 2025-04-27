package com.meera.db.typeconverters.notifications

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.meera.db.models.notifications.UserEntity

class ConvertToListUserEntityNotification {

    companion object {

        @TypeConverter
        @JvmStatic
        fun fromList(listUserEntity: List<UserEntity>?): String? {
            if (listUserEntity == null) {
                return null
            }
            return Gson().toJson(listUserEntity)
        }

        @TypeConverter
        @JvmStatic
        fun toList(listUserEntityJson: String?): List<UserEntity>? {
            if (listUserEntityJson == null) {
                return null
            }
            val listUserEntityType = object : TypeToken<List<UserEntity>>() {}.type
            return Gson().fromJson(listUserEntityJson, listUserEntityType)
        }
    }

}
