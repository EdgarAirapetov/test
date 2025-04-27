package com.meera.db.typeconverters.userprofile

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.meera.db.models.userprofile.GiftEntity


class ConvertToListGiftEntity {

    companion object {

        @TypeConverter
        @JvmStatic
        fun fromListGifts(gifts: List<GiftEntity>?) : String? {
            if (gifts == null) {
                return null
            }
            return Gson().toJson(gifts)
        }

        @TypeConverter
        @JvmStatic
        fun toListGifts(giftsJson: String?) : List<GiftEntity>? {
            if (giftsJson == null) {
                return emptyList()
            }
            val giftsType = object : TypeToken<List<GiftEntity>>() {}.type
            return Gson().fromJson(giftsJson, giftsType)
        }
    }

}
