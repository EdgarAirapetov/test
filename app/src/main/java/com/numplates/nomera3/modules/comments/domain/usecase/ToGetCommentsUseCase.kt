package com.numplates.nomera3.modules.comments.domain.usecase

import com.numplates.nomera3.modules.comments.data.api.OrderType
import com.numplates.nomera3.modules.comments.data.api.SortByType
import com.numplates.nomera3.modules.comments.data.entity.CommentsEntityResponse
import com.numplates.nomera3.modules.comments.data.repository.PostCommentsRepository
import com.numplates.nomera3.modules.comments.domain.BaseUseCaseCoroutine
import com.numplates.nomera3.modules.comments.domain.DefParams
import javax.inject.Inject

private const val LIMIT = 50L
private const val INNER_LIMIT = 10L

class ToGetCommentsUseCase @Inject constructor(
        private val repository: PostCommentsRepository
) : BaseUseCaseCoroutine<ToGetCommentParams, CommentsEntityResponse> {

    override suspend fun execute(params: ToGetCommentParams,
                                 success: (CommentsEntityResponse) -> Unit,
                                 fail: (Exception) -> Unit) {
        val limit = if(params.parentId == null) LIMIT else INNER_LIMIT

        repository.fetchComments(
                postId = params.postId,
                limit = limit,
                startId = params.startId,
                parentId = params.parentId,
                commentId = params.commentId,
                order = params.order,
                sortBy = params.sortBy,
                success = success,
                fail = fail
        )
    }

}

data class ToGetCommentParams(
        val postId: Long,
        val limit: Long = LIMIT,
        val startId: Long? = null,
        val parentId: Long? = null,
        val commentId: Long? = null,
        val order: OrderType = OrderType.AFTER,
        val sortBy: SortByType = SortByType.TIMESTAMP,
) : DefParams()
