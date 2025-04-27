package com.numplates.nomera3.modules.peoples.data.dto

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.meera.db.models.userprofile.City
import com.meera.db.models.userprofile.Country
import com.meera.db.models.userprofile.UserSettingsFlags
import kotlinx.parcelize.Parcelize

@Parcelize
data class RelatedUsersDto(
    @SerializedName("user_id")
    val id: Long?,
    @SerializedName("name")
    val name: String?,
    @SerializedName("account_color")
    val accountColor: Int?,
    @SerializedName("account_type")
    val accountType: Int?,
    @SerializedName("approved")
    val approved: Int?,
    @SerializedName("avatar_small")
    val avatar: String?,
    @SerializedName("birthday")
    val birthday: Long?,
    @SerializedName("city")
    val city: City?,
    @SerializedName("country")
    val country: Country?,
    @SerializedName("country_id")
    val countryId: Long?,
    @SerializedName("gender")
    val gender: Int?,
    @SerializedName("settings_flags")
    val settingsFlags: UserSettingsFlags?,
    @SerializedName("mutual_users")
    val mutualFriends: List<RelatedUsersDto>?,
    @SerializedName("mutual_total_count")
    val mutualTotalCount: Int?,
    @SerializedName("top_content_maker")
    val topContentMaker: Int?
) : Parcelable
