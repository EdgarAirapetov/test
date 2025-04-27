package com.numplates.nomera3.modules.reactionStatistics.data.entity

import com.google.gson.annotations.SerializedName

data class UserDto(
    @SerializedName("account_color") val accountColor: Int,
    @SerializedName("account_type") val accountType: Int,
    @SerializedName("approved") val approved: Int,
    @SerializedName("avatar") val avatar: String,
    @SerializedName("birthday") val birthday: Long,
    @SerializedName("city") val city: String?,
    @SerializedName("gender") val gender: Int,
    @SerializedName("id") val id: Long,
    @SerializedName("name") val name: String?,
    @SerializedName("top_content_maker") val topContentMaker: Int,
    @SerializedName("uniqname") val uniqname: String?,
)
