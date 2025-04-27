package com.meera.db.typeconverters.userprofile

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.meera.db.models.userprofile.ProductEntity


class ConvertToProductEntity {
    companion object {

        @TypeConverter
        @JvmStatic
        fun fromVehicle(product: ProductEntity?): String? {
            if (product == null) {
                return null
            }
            return Gson().toJson(product)
        }

        @TypeConverter
        @JvmStatic
        fun toVehicle(productJson: String?): ProductEntity? {
            if (productJson == null) {
                return null
            }
            val vehicleType = object : TypeToken<ProductEntity>() {}.type
            return Gson().fromJson(productJson, vehicleType)
        }
    }
}
