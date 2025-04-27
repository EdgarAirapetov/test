package com.meera.db.typeconverters.people

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.meera.db.models.people.PeopleUserPostDbModel
import java.lang.reflect.Type

class PeopleUserPostTypeConvertor {

    companion object {

        @JvmStatic
        @TypeConverter
        fun fromPeopleUserPosts(posts: List<PeopleUserPostDbModel>?): String? {
            return if (posts.isNullOrEmpty()) {
                null
            } else {
                Gson().toJson(posts)
            }
        }

        @JvmStatic
        @TypeConverter
        fun toPeopleUserPosts(postsJson: String?): List<PeopleUserPostDbModel>? {
            return postsJson?.let {
                val typeToken: Type = object: TypeToken<List<PeopleUserPostDbModel>>() {}.type
                Gson().fromJson(postsJson, typeToken)
            } ?: run {
                null
            }
        }
    }
}
