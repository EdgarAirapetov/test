package com.numplates.nomera3.modules.newroads.data.typeconverters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.meera.db.models.message.ParsedUniquename


object ParsedPostUniqnameConverter {
    @TypeConverter
    @JvmStatic
    fun fromParsedPostUniqname(tag: ParsedUniquename?): String? {
        if (tag == null) {
            return null
        }
        return Gson().toJson(tag)
    }

    @TypeConverter
    @JvmStatic
    fun toParsedPostUniqname(tagJson: String?): ParsedUniquename? {
        if (tagJson == null) {
            return null
        }
        val tagType = object : TypeToken<ParsedUniquename>() {}.type
        return Gson().fromJson(tagJson, tagType)
    }
}

