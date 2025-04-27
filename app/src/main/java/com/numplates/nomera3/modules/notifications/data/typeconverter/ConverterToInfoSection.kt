package com.numplates.nomera3.modules.notifications.data.typeconverter

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.meera.db.models.notifications.InfoSectionEntity

object ConverterToInfoSection {

    @TypeConverter
    @JvmStatic
    fun from(entity: InfoSectionEntity?): String? {
        entity ?: return null
        return Gson().toJson(entity)
    }

    @TypeConverter
    @JvmStatic
    fun to(json: String?): InfoSectionEntity? {
        json ?: return null
        val type = object : TypeToken<InfoSectionEntity>() {}.type
        return Gson().fromJson(json, type)
    }

}

