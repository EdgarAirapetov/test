package com.meera.db.typeconverters.people

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.meera.db.models.MutualUserDbModel
import java.lang.reflect.Type

class MutualUsersTypeConvertor {

    companion object {

        @JvmStatic
        @TypeConverter
        fun fromMutualUsers(mutualUsers: List<MutualUserDbModel>?): String? {
            return if (mutualUsers.isNullOrEmpty()) {
                null
            } else {
                Gson().toJson(mutualUsers)
            }
        }

        @JvmStatic
        @TypeConverter
        fun toMutualUsers(mutualUsersJson: String?): List<MutualUserDbModel>? {
            return if (mutualUsersJson.isNullOrEmpty()) {
                null
            } else {
                val type: Type = object : TypeToken<List<MutualUserDbModel>>() {}.type
                Gson().fromJson(mutualUsersJson, type)
            }
        }
    }
}
