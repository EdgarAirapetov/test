package com.numplates.nomera3.modules.bump.data.entity

import com.google.gson.annotations.SerializedName
import com.meera.db.models.userprofile.City
import com.meera.db.models.userprofile.Country

data class UserShakeDtoModel(
    @SerializedName("user_id")
    val userId: Long,
    @SerializedName("name")
    val name: String?,
    @SerializedName("uniqname")
    val uniqueName: String?,
    @SerializedName("birthday")
    val birthday: Long?,
    @SerializedName("avatar_small")
    val avatarSmall: String?,
    @SerializedName("gender")
    val gender: Int?,
    @SerializedName("account_type")
    val accountType: Int?,
    @SerializedName("account_color")
    val accountColor: Int?,
    @SerializedName("approved")
    val approved: Int?,
    @SerializedName("top_content_maker")
    val topContentMaker: Int?,
    @SerializedName("complete")
    val complete: Int?,
    @SerializedName("city")
    val city: City?,
    @SerializedName("country")
    val country: Country?,
    @SerializedName("is_friend")
    val isFriends: Int?,
    @SerializedName("mutual")
    val mutualUsers: ShakeMutualUsersDto?
) {
    val hasMutualUsers: Boolean
        get() = mutualUsers?.users.isNullOrEmpty().not()
}
