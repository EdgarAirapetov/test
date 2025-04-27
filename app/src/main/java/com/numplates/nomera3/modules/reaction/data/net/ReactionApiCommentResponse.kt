package com.numplates.nomera3.modules.reaction.data.net

import com.google.gson.annotations.SerializedName
import com.numplates.nomera3.modules.comments.data.entity.CommentEntityResponse
import com.numplates.nomera3.modules.feed.data.entity.PostEntityResponse
import com.numplates.nomera3.modules.moments.show.data.MomentItemDto

data class ReactionApiCommentResponse(
    @SerializedName("comment")
    val comment: CommentEntityResponse
)

data class ReactionApiPostResponse(
    @SerializedName("post")
    val post: PostEntityResponse
)

data class ReactionApiMomentResponse(
    @SerializedName("moment")
    val moment: MomentItemDto
)
