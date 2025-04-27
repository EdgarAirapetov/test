package com.numplates.nomera3.modules.userprofile.data.entity

import com.google.gson.annotations.SerializedName

data class MutualUsersDto(
    @SerializedName("user_ids")
    val userIds: List<Int>? = null,

    @SerializedName("users")
    val userSimple: List<UserSimpleDto>? = null,

    @SerializedName("more_count")
    val moreCount: Int? = 0
)
