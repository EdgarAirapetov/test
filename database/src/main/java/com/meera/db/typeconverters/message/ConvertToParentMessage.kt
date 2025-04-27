package com.meera.db.typeconverters.message

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.meera.db.models.message.ParentMessage

class ConvertToParentMessage {

    companion object {

        @TypeConverter
        @JvmStatic
        fun fromParentMessage(message: ParentMessage?): String? {
            if (message == null) {
                return null
            }
            return Gson().toJson(message)
        }

        @TypeConverter
        @JvmStatic
        fun toParentMessage(messageJson: String?): ParentMessage? {
            if (messageJson == null) {
                return null
            }
            val messageType = object : TypeToken<ParentMessage>() {}.type
            return Gson().fromJson(messageJson, messageType)
        }
    }


}
