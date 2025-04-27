package com.meera.db.typeconverters.dialog

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.meera.db.models.dialog.DialogStyle

class ConvertToDialogStyle {
    companion object {

        @TypeConverter
        @JvmStatic
        fun fromDialogStyle(style: DialogStyle?): String? {
            if (style == null) {
                return null
            }
            return Gson().toJson(style)
        }

        @TypeConverter
        @JvmStatic
        fun toDialogStyle(styleJson: String?): DialogStyle? {
            if (styleJson == null) {
                return null
            }
            val dialogStyle = object : TypeToken<DialogStyle>() {}.type
            return Gson().fromJson(styleJson, dialogStyle)
        }
    }
}
