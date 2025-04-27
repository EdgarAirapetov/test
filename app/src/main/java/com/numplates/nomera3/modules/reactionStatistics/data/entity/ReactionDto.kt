package com.numplates.nomera3.modules.reactionStatistics.data.entity

import com.google.gson.annotations.SerializedName

data class ReactionDto(
    @SerializedName("reaction") val reaction: String,
    @SerializedName("comment_id") val commentId: Long,
    @SerializedName("post_id") val postId: Long,
    @SerializedName("user_id") val userId: Long,
    @SerializedName("created_at") val createdAt: Long,
    @SerializedName("updated_at") val updatedAt: Long,
    @SerializedName("user") val user: UserDto
)
