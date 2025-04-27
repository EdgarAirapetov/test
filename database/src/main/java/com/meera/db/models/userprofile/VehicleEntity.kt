package com.meera.db.models.userprofile

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class VehicleEntity(

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
    val type: VehicleType?,

    @SerializedName("brand")
    val brand: VehicleBrand?,

    @SerializedName("model")
    val model: VehicleModel?,

    @SerializedName("country")
    val country: VehicleCountry?,

    @SerializedName("icon")
    val brandIcon: String? = null

): Parcelable {

    constructor(number: String, type: VehicleType,  country: VehicleCountry) :
        this(0L, number, "", "", "",
            false, "", type, null, null, country)
}

@Parcelize
data class VehicleType(

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
    val icon: String
): Parcelable {
    constructor(typeId: Int) :
        this(typeId, "", false, false, false, "")

    companion object {
        const val TYPE_ID_CAR = 1
        const val TYPE_ID_MOTO = 2
    }
}

@Parcelize
data class VehicleBrand(

    @SerializedName("brand_id")
    val brandId: Int,

    @SerializedName("name")
    val name: String,

    @SerializedName("logo")
    val logo: String
): Parcelable

@Parcelize
data class VehicleModel(

    @SerializedName("model_id")
    val modelId: Int,

    @SerializedName("name")
    val name: String,

    @SerializedName("brand_id")
    val brandId: Int
): Parcelable

@Parcelize
data class VehicleCountry(

    @SerializedName("country_id")
    val countryId: Long,

    @SerializedName("name")
    val name: String,

    @SerializedName("flag")
    val flag: String
): Parcelable {
    constructor(countryId: Long) : this(countryId, "", "")
}

