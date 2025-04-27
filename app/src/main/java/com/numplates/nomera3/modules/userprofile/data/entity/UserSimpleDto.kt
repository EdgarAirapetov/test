package com.numplates.nomera3.modules.userprofile.data.entity

import androidx.room.ColumnInfo
import com.google.gson.annotations.SerializedName
import com.meera.db.models.moments.UserMomentsDto
import com.meera.db.models.userprofile.UserGeo
import com.meera.db.models.userprofile.VehicleEntity
import com.numplates.nomera3.modules.baseCore.data.model.CityDto
import com.numplates.nomera3.modules.baseCore.data.model.CountryDto

data class UserSimpleDto(

    @SerializedName("user_id")
    val userId: Long,

    @SerializedName("name")
    val name: String? = "",

    @SerializedName("birthday")
    val birthday: Long? = null, // если запретили показ возраста то null

    @SerializedName("avatar_small")
    val avatarSmall: String? = "",

    @SerializedName("gender")
    val gender: Int? = -1,

    @SerializedName("account_type")
    val accountType: Int? = 0,

    @SerializedName("account_color")
    val accountColor: Int? = 0,

    @SerializedName("city")
    @ColumnInfo(name = "city")
    val city: CityDto?,

    @SerializedName("country")
    val country: CountryDto?,

    @SerializedName("main_vehicle")
    val mainVehicle: VehicleEntity?,

    @SerializedName("profile_deleted")
    val profileDeleted: Int? = 0,

    @SerializedName("profile_blocked")
    val profileBlocked: Int? = 0,

    @SerializedName("profile_verified")
    val profileVerified: Int? = 0,

    @SerializedName("group_type")// [Модератор, и т.д.]
    val groupType: Int? = 0,

    // Заблокировали меня
    @SerializedName("blacklisted_me")
    val blacklistedMe: Int?,

    // Заблокировали меня
    @SerializedName("blacklisted_by_me")
    val blacklistedByMe: Int?,

    @SerializedName("settings_flags")
    var settingsFlags: UserSettingsFlagsDto?,

    @SerializedName("uniqname")
    val uniqueName: String? = null,

    @SerializedName("system_admin")
    var isSystemAdministrator: Boolean = false,

    @SerializedName("approved")
    val approved: Int = 0,

    @SerializedName("top_content_maker")
    val topContentMaker: Int = 0,

    @SerializedName("mutual_friends_count")
    val mutualFriendsCount: Int? = null,

    @SerializedName("geo")
    val geo: UserGeo? = null,

    @SerializedName("moments")
    val moments: UserMomentsDto? = null,
)
