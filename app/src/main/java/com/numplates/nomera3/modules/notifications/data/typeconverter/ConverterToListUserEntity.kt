package com.numplates.nomera3.modules.notifications.data.typeconverter

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.meera.db.models.notifications.UserEntity

object ConverterToListUserEntity {
    @TypeConverter
    @JvmStatic
    fun from(list: List<UserEntity>?): String? {
        list ?: return null
        return Gson().toJson(list)
    }

    @TypeConverter
    @JvmStatic
    fun to(json: String?): List<UserEntity>? {
        json ?: return null
        val type = object : TypeToken<List<UserEntity>>() {}.type
        return Gson().fromJson(json, type)
    }
}

