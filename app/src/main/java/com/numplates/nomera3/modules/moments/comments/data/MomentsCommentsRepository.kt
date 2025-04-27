package com.numplates.nomera3.modules.moments.comments.data

import com.numplates.nomera3.data.network.core.ResponseWrapper
import com.numplates.nomera3.modules.comments.data.api.OrderType
import com.numplates.nomera3.modules.comments.data.entity.CommentsEntityResponse
import com.numplates.nomera3.modules.comments.data.entity.SendCommentResponse

interface MomentsCommentsRepository {
    suspend fun sendComment(
        momentItemId: Long,
        text: String,
        commentId: Long,
    ): ResponseWrapper<SendCommentResponse>

    suspend fun getComments(
        momentItemId: Long,
        limit: Long,
        startId: Long? = null,
        parentId: Long? = null,
        commentId: Long? = null,
        order: OrderType? = null
    ): ResponseWrapper<CommentsEntityResponse?>
}
