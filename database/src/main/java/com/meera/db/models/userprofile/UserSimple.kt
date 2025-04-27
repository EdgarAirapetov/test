package com.meera.db.models.userprofile

import android.os.Parcelable
import androidx.room.ColumnInfo
import com.google.gson.annotations.SerializedName
import com.meera.db.models.moments.UserMomentsDto
import kotlinx.parcelize.Parcelize
import java.io.Serializable

@Parcelize
data class UserSimple(

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
    val city: City?,

    @SerializedName("country")
    val country: Country?,

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
    var settingsFlags: UserSettingsFlags?,

    @SerializedName("uniqname")
    val uniqueName: String? = null,

    @SerializedName("system_admin")
    var isSystemAdministrator: Boolean = false,

    @SerializedName("approved")
    val approved: Int = 0,

    @SerializedName("top_content_maker")
    val topContentMaker: Int = 0,

    var isChecked: Boolean = false,

    @SerializedName("mutual_friends_count")
    val mutualFriendsCount: Int? = null,

    @SerializedName("moments")
    val moments: UserMomentsDto? = null,

    @SerializedName("geo")
    val geo: UserGeo? = null,

    ) : Serializable, Parcelable {

    constructor(userId: Long, avatar: String, name: String, approved: Int) :
        this(
            0, name, 0, avatar, 0, 0, 0,
            null, null, null, 0, 0, 0,
            0, 0, 0, null, approved = approved
        )

    constructor(
        userId: Long, avatar: String, name: String, uniqueName: String?,
        accountColor: Int?, accountType: Int?, birthday: Long?, city: City?, approved: Int
    ) :
        this(
            userId, name, birthday, avatar, 0, accountType, accountColor,
            city, null, null, 0, 0, 0,
            0, 0, 0, null, uniqueName = uniqueName, approved = approved
        )

    constructor(user: UserModel) : this(
        user.userId,
        user.name,
        user.birthday,
        user.avatar,
        user.gender,
        user.accountType,
        user.accountColor,
        City(0, user.city),
        Country(0, ""),
        null,
        0,
        0,
        0,
        0,
        0,
        0,
        user.settingsFlags,
        uniqueName = user.uniqueName,
        approved = user.approved,
    )

    // For search People
    constructor(
        userId: Long,
        avatar: String,
        name: String,
        blacklistedByMe: Int,
        approved: Int
    ) :
        this(
            userId, name, 0, avatar, 0, 0, 0,
            null, null, null, 0, 0, 0,
            0, 0, blacklistedByMe, null, approved = approved
        )

}

data class UserModel(@SerializedName("user_id") var userId: Long,
                     @SerializedName("name") var name: String,
                     @SerializedName("birthday") var birthday: Long?,
                     @SerializedName("avatar") var avatar: String?,
                     @SerializedName("account_type") var accountType: Int,
                     @SerializedName("account_color") var accountColor: Int?,
                     @SerializedName("gender") var gender: Int,
                     var isChecked: Boolean = false,
                     @SerializedName("city") var city: String? = "",
                     var settingsFlags: UserSettingsFlags? = null,
                     @SerializedName("uniqname") val uniqueName: String? = null,
                     @SerializedName("approved") val approved: Int = 0
) : Serializable {
    constructor(userSimple: UserSimple) : this(
        userSimple.userId, userSimple.name?:"", userSimple.birthday,
        userSimple.avatarSmall, userSimple.accountType?: 0,
        userSimple.accountColor, userSimple.gender?:0, false,
        userSimple.city?.name, userSimple.settingsFlags
    )
}
