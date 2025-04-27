package com.numplates.nomera3.modules.newroads.data.typeconverters

import androidx.room.TypeConverter
import com.meera.db.models.message.UniquenameEntity

// TODO: 02.03.2021 Костыль из-за ошибки room (выдает ошибку на @Ignore)

object ListPostTagsEntityConverter {

    @TypeConverter
    @JvmStatic
    fun fromPostTagsEntity(tags: List<UniquenameEntity?>?): String? {
        if (tags == null) {
            return null
        }
        return ""
    }

    @TypeConverter
    @JvmStatic
    fun toPostTagsEntity(tagsJson: String?): List<UniquenameEntity?>? {
        if (tagsJson == null) {
            return null
        }
        return mutableListOf()
    }
}

