package com.numplates.nomera3.modules.moments.comments.domain

import com.numplates.nomera3.modules.comments.data.api.OrderType
import com.numplates.nomera3.modules.comments.data.entity.CommentsEntityResponse
import com.numplates.nomera3.modules.moments.show.data.MomentsRepository
import javax.inject.Inject

private const val LIMIT = 50L
private const val INNER_LIMIT = 10L

private const val MOMENT_ACCESS_RESTRICTED = 2

class MomentGetCommentsUseCase @Inject constructor(
    private val repository: MomentsRepository
) {
    suspend fun invoke(
        momentItemId: Long,
        startId: Long? = null,
        parentId: Long? = null,
        commentId: Long? = null,
        order: OrderType = OrderType.AFTER,
    ): CommentsEntityResponse {
        val resultLimit = if (parentId == null) LIMIT else INNER_LIMIT

        val result = repository.getComments(
            momentItemId = momentItemId,
            limit = resultLimit,
            startId = startId,
            parentId = parentId,
            commentId = commentId,
            order = order,
        )

        when (result.err?.code) {
            MOMENT_ACCESS_RESTRICTED -> {
                throw MomentAccessRestrictedException(result.err?.message)
            }
        }

        return result.data!!
    }

    data class MomentAccessRestrictedException(val userMessage: String?) : Exception()
}
