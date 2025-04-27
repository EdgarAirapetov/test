package com.numplates.nomera3.modules.comments.data.repository

import com.numplates.nomera3.modules.comments.data.api.OrderType
import com.numplates.nomera3.modules.comments.data.api.SortByType
import com.numplates.nomera3.modules.comments.data.entity.CommentsEntityResponse
import com.numplates.nomera3.modules.comments.data.entity.SendCommentResponse

interface PostCommentsRepository {

    suspend fun commentComplain(commentId: Long): Boolean

    suspend fun momentCommentComplain(commentId: Long): Boolean

    suspend fun fetchComments(
        postId: Long,
        limit: Long,
        startId: Long? = null,
        parentId: Long? = null,
        commentId: Long? = null,
        order: OrderType = OrderType.AFTER,
        sortBy: SortByType = SortByType.TIMESTAMP,
        success: (CommentsEntityResponse) -> Unit,
        fail: (Exception) -> Unit
    )

    suspend fun deletePostComment(commentId: Long)

    suspend fun sendComment(
        postId: Long,
        text: String,
        commentId: Long,
        errorTypeListener: (SendCommentError) -> Unit
    ): SendCommentResponse?

    suspend fun requestLastComments(
        postId: Long,
        limit: Long,
        fail: (Exception) -> Unit,
        success: (CommentsEntityResponse?) -> Unit
    )

}
