package com.meera.db.typeconverters.notifications

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.meera.db.models.notifications.MetaNotificationEntity


class ConvertToMetaNotificationEntity {

    companion object {

        @TypeConverter
        @JvmStatic
        fun fromMetaNotificationEntity(metaNotificationEntity: MetaNotificationEntity?): String? {
            if (metaNotificationEntity == null) {
                return null
            }
            return Gson().toJson(metaNotificationEntity)
        }

        @TypeConverter
        @JvmStatic
        fun toMetaNotificationEntity(metaNotificationEntityJson: String?): MetaNotificationEntity? {
            if (metaNotificationEntityJson == null) {
                return null
            }
            val metaNotificationEntityType = object : TypeToken<MetaNotificationEntity>() {}.type
            return Gson().fromJson(metaNotificationEntityJson, metaNotificationEntityType)
        }
    }

}
