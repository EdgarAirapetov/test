package com.numplates.nomera3.modules.feed.data.entity

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.meera.db.models.moments.UserMomentsDto
import kotlinx.parcelize.Parcelize

@Parcelize
data class UserEntityResponse(
        @SerializedName("user_id")
        val userId: Long,

        @SerializedName("account_type")
        val accountType: Int,

        @SerializedName("name")
        val name: String,

        @SerializedName("avatar_small")
        val avatar: String?,

        @SerializedName("gender")
        val gender: Int,

        @SerializedName("account_color")
        val accountColor: Int,

        @SerializedName("birthday")
        val birthday: Long?,

        @SerializedName("approved")
        val approved: Int = 0,

        @SerializedName("top_content_maker")
        val topContentMaker: Int = 0,

        @SerializedName("system_admin")
        var isSystemAdministrator: Boolean = false,

        // Заблокировали меня
        @SerializedName("blacklisted_me")
        val blacklistedMe: Int?,

        // Заблокировали меня
        @SerializedName("blacklisted_by_me")
        val blacklistedByMe: Int?,

        @SerializedName("settings_flags")
        val settingsFlags: SettingsFlags,

        @SerializedName("moments")
        val moments: UserMomentsDto?

): Parcelable

@Parcelize
data class SettingsFlags(
        @SerializedName("subscription_on")
        val subscriptionOnUser: Int?,

        @SerializedName("subscribed_to_me")
        val subscribedToMe: Int?,

        @SerializedName("friend_status")
        val friendStatus: Int?
):Parcelable
