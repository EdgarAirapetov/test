package com.meera.db.typeconverters.chatmembers

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.meera.db.models.chatmembers.UserEntity

class ConvertToUserEntity {

    companion object {

        @TypeConverter
        @JvmStatic
        fun fromUserEntity(userEntity: UserEntity?): String? {
            if (userEntity == null) {
                return null
            }
            return Gson().toJson(userEntity)
        }

        @TypeConverter
        @JvmStatic
        fun toUserEntity(userEntityJson: String?): UserEntity? {
            if (userEntityJson == null) {
                return null
            }
            val userEntityType = object : TypeToken<UserEntity>() {}.type
            return Gson().fromJson(userEntityJson, userEntityType)
        }
    }

}
