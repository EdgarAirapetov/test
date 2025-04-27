package com.numplates.nomera3.modules.userprofile.data.entity

import com.google.gson.annotations.SerializedName
import com.numplates.nomera3.data.network.Post

data class AvatarDto(
    @SerializedName("animation") val animation: String?,
    @SerializedName("big") val big: String,
    @SerializedName("id") val id: Long,
    @SerializedName("main") val main: Int,
    @SerializedName("post") val post: Post?,
    @SerializedName("post_id") val postId: Long?,
    @SerializedName("small") val small: String,
    @SerializedName("user_id") val userId: Long,
    @SerializedName("is_adult") val isAdult: Int?,
)
