package com.meera.db.typeconverters.message

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class CovertToListIntRanges {

    companion object {

        @TypeConverter
        @JvmStatic
        fun fromListIntRange(listIntRanges: List<IntRange>?): String? {
            if (listIntRanges == null) {
                return null
            }
            return Gson().toJson(listIntRanges)
        }

        @TypeConverter
        @JvmStatic
        fun toListIntRange(listIntRangesJson: String?): List<IntRange>? {
            if (listIntRangesJson == null) {
                return null
            }
            val listIntRangesType = object : TypeToken<List<IntRange>>() {}.type
            return Gson().fromJson(listIntRangesJson, listIntRangesType)
        }
    }

}
