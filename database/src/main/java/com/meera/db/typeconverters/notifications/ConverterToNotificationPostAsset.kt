package com.meera.db.typeconverters.notifications

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.meera.db.models.PostAsset

class ConverterToNotificationPostAsset {
    companion object {
        @TypeConverter
        @JvmStatic
        fun from(entity: PostAsset?): String? {
            entity ?: return null
            return Gson().toJson(entity)
        }

        @TypeConverter
        @JvmStatic
        fun to(json: String?): PostAsset? {
            json ?: return null
            val type = object : TypeToken<PostAsset>() {}.type
            return Gson().fromJson(json, type)
        }
    }
}
