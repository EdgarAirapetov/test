package com.meera.db.typeconverters.userprofile

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.meera.db.models.userprofile.GroupEntity

class ConvertToListGroups {

    companion object {

        @TypeConverter
        @JvmStatic
        fun fromGroupEntities(groups: List<GroupEntity>?) : String? {
            if (groups == null) {
                return null
            }
            return Gson().toJson(groups)
        }

        @TypeConverter
        @JvmStatic
        fun toGroupEntities(groupsJson: String?) : List<GroupEntity>? {
            if (groupsJson == null) {
                return emptyList()
            }
            val groupsType = object : TypeToken<List<GroupEntity>>() {}.type
            return Gson().fromJson(groupsJson, groupsType)
        }
    }

}
