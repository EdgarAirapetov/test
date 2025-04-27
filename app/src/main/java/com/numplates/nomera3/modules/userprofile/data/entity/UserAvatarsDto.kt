package com.numplates.nomera3.modules.userprofile.data.entity

import com.google.gson.annotations.SerializedName

data class UserAvatarsDto(
    @SerializedName("avatars") val avatars: List<AvatarDto>,
    @SerializedName("count") val count: Int,
    @SerializedName("more_items") val moreItems: Int
)
