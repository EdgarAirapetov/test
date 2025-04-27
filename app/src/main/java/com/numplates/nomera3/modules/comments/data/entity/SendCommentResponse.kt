package com.numplates.nomera3.modules.comments.data.entity

import com.google.gson.annotations.SerializedName

data class SendCommentResponse(
    @SerializedName("comment") // my comment
    val myComment: CommentEntityResponse? = null,

    @SerializedName("last_comments") // comments before my comment
    val lastComments: CommentsEntityResponse? = null
)
