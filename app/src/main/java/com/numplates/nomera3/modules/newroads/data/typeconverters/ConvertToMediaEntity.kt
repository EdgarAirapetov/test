package com.numplates.nomera3.modules.newroads.data.typeconverters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.numplates.nomera3.modules.newroads.data.entities.MediaEntity

object ConvertToMediaEntity {

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
