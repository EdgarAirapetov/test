package com.meera.db.typeconverters.message

import androidx.room.TypeConverter
import com.meera.db.models.message.UniquenameEntity

class ConvertToUniquenameEntity {

    companion object {

        @TypeConverter
        @JvmStatic
        fun fromPostTagsEntity(tags: List<UniquenameEntity>?): String? {
            if (tags == null) {
                return null
            }
            return ""
        }

        @TypeConverter
        @JvmStatic
        fun toPostTagsEntity(tagsJson: String?): List<UniquenameEntity>? {
            if (tagsJson == null) {
                return null
            }
            return mutableListOf()
        }
    }

}
