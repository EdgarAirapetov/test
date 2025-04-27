package com.numplates.nomera3.modules.bump.data.entity

import com.google.gson.annotations.SerializedName

data class ShakeMutualUsersDto(
    @SerializedName("user_ids")
    val userIds: List<Int>? = null,

    @SerializedName("users")
    val users: List<ShakeMutualUserDto>? = null,

    @SerializedName("more_count")
    val moreCount: Int? = 0
)
