package com.meera.db.typeconverters.userprofile

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.meera.db.models.moments.UserMomentsDto

class ConvertToUserMoments {

    companion object {

        @TypeConverter
        @JvmStatic
        fun fromUserMomentsDto(userMomentsDto: UserMomentsDto?): String? {
            if (userMomentsDto == null) {
                return null
            }
            return Gson().toJson(userMomentsDto)
        }

        @TypeConverter
        @JvmStatic
        fun toUserMomentsDto(userMomentsDtoJson: String?): UserMomentsDto? {
            if (userMomentsDtoJson == null) {
                return null
            }
            val vehicleEntityType = object : TypeToken<UserMomentsDto>() {}.type
            return Gson().fromJson(userMomentsDtoJson, vehicleEntityType)
        }
    }

}
