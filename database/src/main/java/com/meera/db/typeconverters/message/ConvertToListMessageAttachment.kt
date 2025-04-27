package com.meera.db.typeconverters.message

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.meera.db.models.message.MessageAttachment

class ConvertToListMessageAttachment {

    companion object {

        @TypeConverter
        @JvmStatic
        fun fromMessageAttachment(listMessageAttachment: List<MessageAttachment>?): String? {
            if (listMessageAttachment == null) {
                return null
            }
            return Gson().toJson(listMessageAttachment)
        }

        @TypeConverter
        @JvmStatic
        fun toMessageAttachment(listMessageAttachmentJson: String?): List<MessageAttachment>? {
            if (listMessageAttachmentJson == null) {
                return null
            }
            val listMessageAttachmentType = object : TypeToken<List<MessageAttachment>>() {}.type
            return Gson().fromJson(listMessageAttachmentJson, listMessageAttachmentType)
        }
    }

}
