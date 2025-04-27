package com.meera.db.typeconverters.message

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.meera.db.models.message.ParsedUniquename

class ConvertToParsedUniquename {

    companion object {

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

}
