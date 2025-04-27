package com.meera.db.typeconverters.notifications

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.meera.db.models.notifications.MediaEntity


class ConvertToMediaEntity {

    companion object {

        @TypeConverter
        @JvmStatic
        fun fromMediaEntity(mediaEntity: MediaEntity?): String? {
            if (mediaEntity == null) {
                return null
            }
            return Gson().toJson(mediaEntity)
        }

        @TypeConverter
        @JvmStatic
        fun toCountryEntity(mediaJson: String?): MediaEntity? {
            if (mediaJson == null) {
                return null
            }
            val countryType = object : TypeToken<MediaEntity>() {}.type
            return Gson().fromJson(mediaJson, countryType)
        }
    }

}
