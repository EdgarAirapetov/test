package com.meera.db.typeconverters.userprofile

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.meera.db.models.userprofile.MutualUsersEntity



class TypeConvertorToMutualUsers {

    companion object {
        @TypeConverter
        @JvmStatic
        fun fromMedia(entity: MutualUsersEntity?): String? {
            return if (entity != null) {
                Gson().toJson(entity)
            } else null
        }

        @TypeConverter
        @JvmStatic
        fun toMedia(mutualStJson: String?): MutualUsersEntity? {
            return if (mutualStJson != null) {
                val typeToken = object : TypeToken<MutualUsersEntity>() {}.type
                return Gson().fromJson(mutualStJson, typeToken)
            } else null
        }
    }
}
