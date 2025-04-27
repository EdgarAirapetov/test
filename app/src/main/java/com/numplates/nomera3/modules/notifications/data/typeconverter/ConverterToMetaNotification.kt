package com.numplates.nomera3.modules.notifications.data.typeconverter

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.meera.db.models.notifications.MetaNotificationEntity


object ConverterToMetaNotification {

    @TypeConverter
    @JvmStatic
    fun from(entity: MetaNotificationEntity?): String? {
        entity ?: return null
        return Gson().toJson(entity)
    }

    @TypeConverter
    @JvmStatic
    fun to(json: String?): MetaNotificationEntity? {
        json ?: return null
        val type = object : TypeToken<MetaNotificationEntity>() {}.type
        return Gson().fromJson(json, type)
    }

}

