package com.numplates.nomera3.modules.notifications.data.typeconverter

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.meera.db.models.notifications.UserEntity

object ConverterToUserEntity {

    @TypeConverter
    @JvmStatic
    fun from(entity: UserEntity?): String? {
        entity ?: return null
        return Gson().toJson(entity)
    }

    @TypeConverter
    @JvmStatic
    fun to(json: String?): UserEntity? {
        json ?: return null
        val type = object : TypeToken<UserEntity>() {}.type
        return Gson().fromJson(json, type)
    }
}

