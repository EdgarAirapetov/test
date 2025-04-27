package com.numplates.nomera3.modules.userprofile.data.entity

import com.google.gson.annotations.SerializedName

data class VehicleDto(

    @SerializedName("id")
    val id: Long,

    @SerializedName("number")
    val number: String?,

    @SerializedName("image")
    val image: String?,

    @SerializedName("avatar_big")
    val avatarBig: String?,

    @SerializedName("avatar_small")
    val avatarSmall: String?,

    @SerializedName("is_main")
    val isMain: Boolean?,

    @SerializedName("description")
    val description: String?,

    @SerializedName("type")
    val type: VehicleTypeDto?,

    @SerializedName("brand")
    val brand: VehicleBrandDto?,

    @SerializedName("model")
    val model: VehicleModelDto?,

    @SerializedName("country")
    val country: VehicleCountryDto?,

    @SerializedName("icon")
    val brandIcon: String? = null
)

data class VehicleCountryDto(
    @SerializedName("country_id")
    val countryId: Long,

    @SerializedName("name")
    val name: String,

    @SerializedName("flag")
    val flag: String
)

data class VehicleModelDto(
    @SerializedName("model_id")
    val modelId: Int,

    @SerializedName("name")
    val name: String,

    @SerializedName("brand_id")
    val brandId: Int
)

data class VehicleBrandDto (
    @SerializedName("brand_id")
    val brandId: Int,

    @SerializedName("name")
    val name: String,

    @SerializedName("logo")
    val logo: String?
)

data class VehicleTypeDto(
    @SerializedName("type_id")
    val typeId: Int,

    @SerializedName("name")
    var name: String,

    @SerializedName("has_number")
    val hasNumber: Boolean,

    @SerializedName("has_brands")
    val hasBrands: Boolean,

    @SerializedName("has_models")
    val hasModels: Boolean,

    @SerializedName("icon")
    val icon: String?
)
