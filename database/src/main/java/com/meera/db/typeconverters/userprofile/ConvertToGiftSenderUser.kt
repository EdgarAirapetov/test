package com.meera.db.typeconverters.userprofile

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.meera.db.models.userprofile.GiftSenderUser

class ConvertToGiftSenderUser {

    companion object {

        @TypeConverter
        @JvmStatic
        fun fromGiftSenderUser(giftSenderUser: GiftSenderUser?): String? {
            if (giftSenderUser == null) {
                return null
            }
            return Gson().toJson(giftSenderUser)
        }

        @TypeConverter
        @JvmStatic
        fun toGiftSenderUser(giftSenderUserJson: String?): GiftSenderUser? {
            if (giftSenderUserJson == null) {
                return null
            }
            val giftSenderUserType = object : TypeToken<GiftSenderUser>() {}.type
            return Gson().fromJson(giftSenderUserJson, giftSenderUserType)
        }
    }

}
