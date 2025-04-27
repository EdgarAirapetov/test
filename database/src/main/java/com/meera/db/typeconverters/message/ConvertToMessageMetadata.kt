package com.meera.db.typeconverters.message

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.meera.db.models.message.MessageMetadata

class ConvertToMessageMetadata {

    companion object {

        @TypeConverter
        @JvmStatic
        fun fromMessageMetadata(messageMetadata: MessageMetadata?): String? {
            if (messageMetadata == null) {
                return null
            }
            return Gson().toJson(messageMetadata)
        }

        @TypeConverter
        @JvmStatic
        fun toMessageMetadata(messageMetadataJson: String?): MessageMetadata? {
            if (messageMetadataJson == null) {
                return null
            }
            val messageMetadataType = object : TypeToken<MessageMetadata>() {}.type
            return Gson().fromJson(messageMetadataJson, messageMetadataType)
        }
    }

}
