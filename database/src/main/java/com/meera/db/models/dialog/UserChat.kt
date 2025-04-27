package com.meera.db.models.dialog

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.meera.db.models.moments.UserMomentsDto
import com.meera.db.models.userprofile.UserOnlineStatus
import com.meera.db.models.userprofile.UserRole
import com.meera.db.models.userprofile.UserSettingsFlags
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.RawValue

@Parcelize
data class UserChat(
    @SerializedName("user_id")
    var userId: Long?,

    @SerializedName("name")
    var name: String? = "",

    @SerializedName("avatar_small")
    var avatarSmall: String? = "",

    @SerializedName("online")
    var onlineStatus: @RawValue UserOnlineStatus? = UserOnlineStatus(),

    @SerializedName("can_write_anonymous_messages")
    var canWriteAnonymousMessage: Int? = null,

    @SerializedName("is_anonym")
    var anonymous: Int? = 0,

    @SerializedName("birth_date")
    var birthDate: Long? = 0,

    // Заблокировали меня
    @SerializedName("blacklisted_me")
    var blacklistedMe: Int? = 0,

    // Я заблокировал другого пользователя
    @SerializedName("blacklisted_by_me")
    var blacklistedByMe: Int? = 0,

    @SerializedName("settings_flags")
    var settingsFlags: @RawValue UserSettingsFlags? = UserSettingsFlags(),

    @SerializedName("account_type")
    var accountType: Int = 0,

    @SerializedName("approved")
    var approved: Int = 0,

    @SerializedName("uniqname")
    var uniqueName: String? = null,

    @SerializedName("top_content_maker")
    var topContentMaker: Int? = null,

    @SerializedName("role")
    var role: String? = null,

    @SerializedName("moments")
    var moments: UserMomentsDto? = null

) : Parcelable {

    constructor() : this(0L, "", "", UserOnlineStatus(),0, 0, 0, 0)

}

val UserChat.userRole: UserRole
    get() = when (role) {
        UserRole.USER.value -> UserRole.USER
        UserRole.ANNOUNCE_USER.value -> UserRole.ANNOUNCE_USER
        UserRole.SUPPORT_USER.value -> UserRole.SUPPORT_USER
        UserRole.SYSTEM_ADMIN.value -> UserRole.SYSTEM_ADMIN
        else -> UserRole.USER
    }
