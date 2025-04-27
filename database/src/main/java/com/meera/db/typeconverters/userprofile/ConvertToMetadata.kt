package com.meera.db.typeconverters.userprofile

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class ConvertToMetadata {

    companion object {

        @TypeConverter
        @JvmStatic
        fun fromMetadata(metadata: com.meera.db.models.userprofile.Metadata?): String? {
            if (metadata == null) {
                return null
            }
            return Gson().toJson(metadata)
        }

        @TypeConverter
        @JvmStatic
        fun toMetadata(metadataJson: String?): com.meera.db.models.userprofile.Metadata? {
            if (metadataJson == null) {
                return null
            }
            val metadataType = object : TypeToken<com.meera.db.models.userprofile.Metadata>() {}.type
            return Gson().fromJson(metadataJson, metadataType)
        }
    }

}
