package com.meera.db.typeconverters.userprofile

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.meera.db.models.userprofile.PhotoEntity

class ConvertToListPhotos {

    companion object {

        @TypeConverter
        @JvmStatic
        fun fromPhotoEntities(photos: List<PhotoEntity>?) : String? {
            if (photos == null) {
                return null
            }
            return Gson().toJson(photos)
        }

        @TypeConverter
        @JvmStatic
        fun toPhotoEntities(photosJson: String?) : List<PhotoEntity>? {
            if (photosJson == null) {
                return emptyList()
            }
            val photosType = object : TypeToken<List<PhotoEntity>>() {}.type
            return Gson().fromJson(photosJson, photosType)
        }
    }

}
