package com.numplates.nomera3.modules.reactionStatistics.data.entity

import com.google.gson.annotations.SerializedName

data class ReactionsRootDto(
    @SerializedName("count") val count: Int,
    @SerializedName("more") val more: Int,
    @SerializedName("comment_id") val commentId: Long,
    @SerializedName("post_id") val postId: Long,
    @SerializedName("reaction") val reaction: String,
    @SerializedName("reactions") val reactions: List<ReactionDto>
)
