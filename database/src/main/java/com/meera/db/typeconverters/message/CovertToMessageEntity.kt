package com.meera.db.typeconverters.message

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.meera.db.models.message.MessageEntity

class CovertToMessageEntity {

    companion object {

        @TypeConverter
        @JvmStatic
        fun fromMessageEntity(message: MessageEntity?): String? {
            if (message == null) {
                return null
            }
            return Gson().toJson(message)
        }

        @TypeConverter
        @JvmStatic
        fun toMessageEntity(messageJson: String?): MessageEntity? {
            if (messageJson == null) {
                return null
            }
            val messageType = object : TypeToken<MessageEntity>() {}.type
            return Gson().fromJson(messageJson, messageType)
        }
    }

}
