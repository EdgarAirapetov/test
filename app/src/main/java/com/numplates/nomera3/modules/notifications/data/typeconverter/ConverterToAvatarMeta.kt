package com.numplates.nomera3.modules.notifications.data.typeconverter

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.meera.db.models.notifications.AvatarMetaEntity

object ConverterToAvatarMeta {

    @TypeConverter
    @JvmStatic
    fun from(entity: AvatarMetaEntity?): String? {
        entity ?: return null
        return Gson().toJson(entity)
    }

    @TypeConverter
    @JvmStatic
    fun to(json: String?): AvatarMetaEntity? {
        json ?: return null
        val type = object : TypeToken<AvatarMetaEntity>() {}.type
        return Gson().fromJson(json, type)
    }

}

