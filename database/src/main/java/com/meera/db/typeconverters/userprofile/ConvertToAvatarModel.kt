package com.meera.db.typeconverters.userprofile

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.meera.db.models.userprofile.AvatarModel

class ConvertToAvatarModel {

    companion object {

        @TypeConverter
        @JvmStatic
        fun fromAvatarModel(avatarModel: AvatarModel?): String? {
            if (avatarModel == null) {
                return null
            }
            return Gson().toJson(avatarModel)
        }

        @TypeConverter
        @JvmStatic
        fun toAvatarModel(avatarModelJson: String?): AvatarModel? {
            if (avatarModelJson == null) {
                return null
            }
            val avatarModelType = object : TypeToken<AvatarModel>() {}.type
            return Gson().fromJson(avatarModelJson, avatarModelType)
        }
    }

}
