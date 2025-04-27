package com.numplates.nomera3.data.network

import android.graphics.Bitmap
import com.google.gson.annotations.SerializedName
import com.meera.db.models.userprofile.City
import com.meera.db.models.userprofile.Country
import java.io.Serializable

data class MeeraUserInfoModel(
    @SerializedName("user") var user: User,
    @SerializedName("approved") var approved: Int = 0,
    @SerializedName("is_admin") var isAdmin: Int,
    @SerializedName("is_author") var isAuthor: Int,
    @SerializedName("is_moderator") var isModerator: Int,
    var bitmap: Bitmap?
) : Serializable

data class User(
    @SerializedName("complete") val complete: Long,
    @SerializedName("name") val name: String?,
    @SerializedName("approved") val approved: Int,
    @SerializedName("user_id") val userId: Long,
    @SerializedName("city") val city: City?,
    @SerializedName("country") val country: Country?,
    @SerializedName("role") val role: String,
    @SerializedName("uniqname") val uniqname: String?,
    @SerializedName("gender") val gender: Int,
    @SerializedName("account_type") val accountType: Int,
    @SerializedName("account_color") val accountColor: Int,
    @SerializedName("birthday") val birthday: Long,
    @SerializedName("top_content_maker") val topContentMaker: Int,
    @SerializedName("closed_profile") val closedProfile: Int,
    @SerializedName("group_type") val groupType: Int,
    @SerializedName("avatar_small") val avatarSmall: String?,
    @SerializedName("blacklisted_by_me") val blacklistedByMe: Int,
    @SerializedName("blacklisted_me") val blacklistedMe: Int,
    @SerializedName("profile_blocked") val profileBlocked: Int,
    @SerializedName("profile_deleted") val profileDeleted: Int,
    @SerializedName("profile_verified") val profileVerified: Int,
    @SerializedName("mutual_friends_count") val mutualFriendsCount: Int
)
