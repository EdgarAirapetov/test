package com.meera.db.typeconverters.message

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.meera.db.models.message.ResponseData


class ConvertToResponseData {

    companion object {

        @TypeConverter
        @JvmStatic
        fun fromResponseData(responseData: ResponseData?): String? {
            if (responseData == null) {
                return null
            }
            return Gson().toJson(responseData)
        }

        @TypeConverter
        @JvmStatic
        fun toResponseData(responseDataJson: String?): ResponseData? {
            if (responseDataJson == null) {
                return null
            }
            val responseDataType = object : TypeToken<ResponseData>() {}.type
            return Gson().fromJson(responseDataJson, responseDataType)
        }
    }

}
