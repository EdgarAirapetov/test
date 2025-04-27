package com.numplates.nomera3.modules.peoples.data.dto

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.meera.db.models.moments.UserMomentsDto
import com.meera.db.models.userprofile.UserSettingsFlags
import kotlinx.parcelize.Parcelize

@Parcelize
data class ApprovedUsersDto(
    @SerializedName("user_id")
    val userId: Long?,
    @SerializedName("subscribers_count")
    val subscribersCount: Int?,
    @SerializedName("name")
    val userName: String?,
    @SerializedName("account_type")
    val accountType: Int?,
    @SerializedName("approved")
    val approved: Int,
    @SerializedName("account_color")
    val accountColor: Int,
    @SerializedName("top_content_maker")
    val topContentMarker: Int,
    @SerializedName("posts")
    val posts: List<ApprovedUserPostDto>?,
    @SerializedName("avatar_small")
    val avatar: String?,
    @SerializedName("uniqname")
    val uniqueName: String?,
    @SerializedName("settings_flags")
    val settingsFlags: UserSettingsFlags?,
    @SerializedName("moments")
    val moments: UserMomentsDto?
) : Parcelable
