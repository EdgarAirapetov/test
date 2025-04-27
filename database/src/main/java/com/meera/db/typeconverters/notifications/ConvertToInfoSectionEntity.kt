package com.meera.db.typeconverters.notifications

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.meera.db.models.notifications.InfoSectionEntity

class ConvertToInfoSectionEntity {

    companion object {

        @TypeConverter
        @JvmStatic
        fun fromInfoSectionEntity(infoSectionEntity: InfoSectionEntity?): String? {
            if (infoSectionEntity == null) {
                return null
            }
            return Gson().toJson(infoSectionEntity)
        }

        @TypeConverter
        @JvmStatic
        fun toInfoSectionEntity(infoSectionEntityJson: String?): InfoSectionEntity? {
            if (infoSectionEntityJson == null) {
                return null
            }
            val infoSectionEntityType = object : TypeToken<InfoSectionEntity>() {}.type
            return Gson().fromJson(infoSectionEntityJson, infoSectionEntityType)
        }
    }

}
