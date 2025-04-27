package com.numplates.nomera3.modules.bump.data.entity

import com.google.gson.annotations.SerializedName
import com.numplates.nomera3.modules.registration.data.Avatar

data class ShakeMutualUserDto(
    @SerializedName("user_id") val userId: Long,
    @SerializedName("name") val name: String?,
    @SerializedName("avatar") val avatar: Avatar?
)
