package com.numplates.nomera3.modules.maps.data.model

import com.google.gson.annotations.SerializedName
import com.meera.db.models.moments.UserMomentsDto
import com.numplates.nomera3.modules.baseCore.data.model.CityDto
import com.numplates.nomera3.modules.baseCore.data.model.CoordinatesDto
import com.numplates.nomera3.modules.baseCore.data.model.CountryDto

data class UserCardDto(
    @SerializedName("user_id") val uid: Long,
    @SerializedName("name") val name: String?,
    @SerializedName("uniqname") val uniqueName: String?,
    @SerializedName("birthday") val birthday: Long?,
    @SerializedName("avatar_small") val avatarSmall: String?,
    @SerializedName("avatar_big") val avatarBig: String?,
    @SerializedName("gender") val gender: Int?,
    @SerializedName("distance") val distance: Double,
    @SerializedName("account_type") val accountType: Int,
    @SerializedName("account_color") val accountColor: Int?,
    @SerializedName("city") val city: CityDto?,
    @SerializedName("country") val countryDto: CountryDto?,
    @SerializedName("profile_deleted") val profileDeleted: Int?,
    @SerializedName("profile_blocked") val profileBlocked: Int?,
    @SerializedName("profile_verified") val profileVerified: Int?,
    @SerializedName("group_type") val groupType: Int?,
    @SerializedName("coordinates") val coordinates: CoordinatesDto,
    @SerializedName("profile_status") val status: String?,
    @SerializedName("approved") val approved: Int?,
    @SerializedName("blacklisted_me") val blacklistedMe: Int?,
    @SerializedName("blacklisted_by_me") val blacklistedByMe: Int?,
    @SerializedName("is_friend") val friendStatus: Int,
    @SerializedName("subscription_on") val subscriptionOn: Int,
    @SerializedName("subscribers_count") val subscribersCount: Long,
    @SerializedName("top_content_maker") val topContentMaker: Int?,
    @SerializedName("moments") val moments: UserMomentsDto? = null
)
