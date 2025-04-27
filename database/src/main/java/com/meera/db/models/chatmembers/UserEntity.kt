package com.meera.db.models.chatmembers

import android.os.Parcelable
import androidx.annotation.Nullable
import com.google.gson.annotations.SerializedName
import com.meera.db.models.moments.UserMomentsDto
import kotlinx.android.parcel.Parcelize


@Parcelize
data class UserEntity(

    @SerializedName("id")
    var userId: Long,

    @Nullable
    @SerializedName("name")
    var name: String?,

    @Nullable
    @SerializedName("avatar_big")
    var avatarBig: String?,

    @Nullable
    @SerializedName("avatar_small")
    var avatarSmall: String?,

    @Nullable
    @SerializedName("birthday")
    var birthday: Long? = null,

    @Nullable
    @SerializedName("city_name")
    var city: String?,

    @SerializedName("color")
    var color: Int?,

    @Nullable
    @SerializedName("gender")
    var gender: Int?,

    @Nullable
    @SerializedName("status")
    var status: String?,

    @SerializedName("type")
    var type: Int?,

    @Nullable
    @SerializedName("email")
    var email: String?,

    @Nullable
    @SerializedName("phone")
    var phone: String?,

    @Nullable
    @SerializedName("anonymous_available")
    val anonymousAvailable: Boolean? = null,

    @Nullable
    @SerializedName("blocked")
    var isBlockedByMe: Boolean? = null,

    @Nullable
    @SerializedName("uniqname")
    val uniqueName: String? = null,

    @Nullable
    @SerializedName("moments")
    val moments: UserMomentsDto? = null,

    @Nullable
    @SerializedName("approved")
    val approved: Int? = null,

    @Nullable
    @SerializedName("top_content_maker")
    var topContentMaker: Int? = null

) : Parcelable {

    constructor() : this(
        userId = 0L,
        name = "",
        avatarBig = "",
        avatarSmall = "",
        birthday = null,
        city = "",
        color = 0,
        gender = 0,
        status ="",
        type = 0,
        email = "",
        phone = "")

    constructor(userId: Long) : this(
        userId = userId,
        name = "",
        avatarBig = "",
        avatarSmall = "",
        birthday = null,
        city = "",
        color = 0,
        gender = 0,
        status ="",
        type = 0,
        email = "",
        phone = "")

    constructor(avatar: String?, type: Int, gender: Int?) : this(
        userId = 0,
        name = "",
        avatarBig = avatar,
        avatarSmall = avatar,
        birthday = null,
        city = "",
        color = 0,
        gender = gender,
        status ="",
        type = type,
        email = "",
        phone = "")

}
