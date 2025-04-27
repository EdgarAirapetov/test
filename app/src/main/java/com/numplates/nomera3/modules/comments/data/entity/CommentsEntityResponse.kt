package com.numplates.nomera3.modules.comments.data.entity

import com.google.gson.annotations.SerializedName

data class CommentsEntityResponse(
        @SerializedName("count_before")
        val countBefore: Int,

        @SerializedName("count_after")
        val countAfter: Int,

        @SerializedName("comments")
        val comments: List<CommentEntityResponse>,
)
