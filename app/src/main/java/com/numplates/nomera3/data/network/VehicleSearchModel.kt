package com.numplates.nomera3.data.network

import com.google.gson.annotations.SerializedName

data class VehicleSearchModel(
        @SerializedName("brand")
        var brand: BrandSmall?,

        @SerializedName("country")
        var country: CountryVeh?,

        @SerializedName("description")
        var description: String?,

        @SerializedName("image")
        var image: String?,

        @SerializedName("is_main")
        var is_main: Int?,

        @SerializedName("model")
        var model: Model?,

        @SerializedName("number")
        var number: String?,

        @SerializedName("type")
        var type: Type?,

        @SerializedName("vehicle_id")
        var vehicle_id: Int?,

        @SerializedName("icon")
        val brandIcon: String? = null
)

data class BrandSmall(
        @SerializedName("brand_id")
        var brandId: Long?,
        @SerializedName("logo")
        var logo: String?,
        @SerializedName("name")
        var name: String?
)

data class CountryVeh(
        @SerializedName("country_id")
        var country_id: Int?,
        @SerializedName("flag")
        var flag: String?,
        @SerializedName("name")
        var name: String?
)

data class Model(
        @SerializedName("brand_id")
        var brand_id: Int?,
        @SerializedName("model_id")
        var model_id: Int?,
        @SerializedName("name")
        var name: String?
)

data class Type(
        @SerializedName("has_brands")
        var has_brands: Int?,
        @SerializedName("has_models")
        var has_models: Int?,
        @SerializedName("has_number")
        var has_number: Int?,
        @SerializedName("icon")
        var icon: String?,
        @SerializedName("name")
        var name: String?,
        @SerializedName("type_id")
        var type_id: Int?
    )
