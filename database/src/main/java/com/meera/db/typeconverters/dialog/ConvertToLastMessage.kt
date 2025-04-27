package com.meera.db.typeconverters.dialog

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.meera.db.models.dialog.LastMessage

class ConvertToLastMessage {

    companion object {

        @TypeConverter
        @JvmStatic
        fun fromLastMessage(message: LastMessage?): String? {
            if (message == null) {
                return null
            }
            return Gson().toJson(message)
        }

        @TypeConverter
        @JvmStatic
        fun toLastMessage(messageJson: String?): LastMessage? {
            if (messageJson == null) {
                return null
            }
            val messageType = object : TypeToken<LastMessage>() {}.type
            return Gson().fromJson(messageJson, messageType)
        }
    }

}
