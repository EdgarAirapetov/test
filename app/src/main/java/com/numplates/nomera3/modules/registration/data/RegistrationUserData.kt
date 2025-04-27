package com.numplates.nomera3.modules.registration.data

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.meera.db.models.usersettings.PrivacySettingDto
import com.numplates.nomera3.data.network.City
import com.numplates.nomera3.data.network.Country
import kotlinx.parcelize.Parcelize

data class RegistrationUserData(
    var name: String? = null,
    var birthday: String? = null,
    var hideAge: Boolean = false,
    var gender: Int? = null,
    var hideGender: Boolean = false,
    var country: Country? = null,
    var city: City? = null,
    var locationAutocomplete: Boolean = false,
    var uniqueName: String? = null,
    var isUniqueNameValid: Boolean = false,
    var avatar: Avatar? = null,
    var photo: String? = null,
    var animatedPhoto: String? = null,
    var avatarAnimation: String? = null,
    var avatarGender: Int? = null,
    var referralCode: String? = null
) {

    fun isLocationExist(): Boolean {
        return country?.countryId != null &&
            country?.countryId != 0 &&
            !country?.name.isNullOrEmpty() &&
            city?.cityId != null &&
            city?.cityId != 0 &&
            city?.countryId != null &&
            city?.countryId != 0 &&
            !city?.title_.isNullOrEmpty()
    }

    companion object {
        const val GENDER_MALE = 1
        const val GENDER_FEMALE = 0
    }
}

@Parcelize
data class Avatar(
    @SerializedName("big")
    val big: String?,
    @SerializedName("small")
    val small: String?,
    @SerializedName("animation")
    val animation: String?
) : Parcelable

data class RegistrationRequest(
    @SerializedName("registration")
    val registration: RegistrationData,
    @SerializedName("settings")
    val settings: List<PrivacySettingDto>?
)

data class RegistrationData(
    @SerializedName("name")
    var name: String?,
    @SerializedName("birthday")
    var birthday: Int?,
    @SerializedName("gender")
    var gender: Int?,
    @SerializedName("country_id")
    var countryId: Int?,
    @SerializedName("city_id")
    var cityId: Int?,
    @SerializedName("uniqname")
    var uniqueName: String?,
    @SerializedName("avatar")
    var avatar: Avatar?
)
