package com.meera.db.typeconverters.notifications

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.meera.db.models.MomentAsset

class ConverterToNotificationMomentAsset {
    companion object {
        @TypeConverter
        @JvmStatic
        fun from(entity: MomentAsset?): String? {
            entity ?: return null
            return Gson().toJson(entity)
        }
        @TypeConverter
        @JvmStatic
        fun to(json: String?): MomentAsset? {
            json ?: return null
            val type = object : TypeToken<MomentAsset>() {}.type
            return Gson().fromJson(json, type)
        }
    }
}
