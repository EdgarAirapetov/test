package com.meera.db.typeconverters.message

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.meera.db.models.message.MessageAttachment


class ConvertToMessageAttachment {

    companion object {

        @TypeConverter
        @JvmStatic
        fun fromMessageAttachment(attachment: MessageAttachment): String {
            return Gson().toJson(attachment)
        }

        @TypeConverter
        @JvmStatic
        fun toMessageAttachment(attachmentJson: String): MessageAttachment {
            val attachmentType = object : TypeToken<MessageAttachment>() {}.type
            return Gson().fromJson(attachmentJson, attachmentType)
        }
    }

}
