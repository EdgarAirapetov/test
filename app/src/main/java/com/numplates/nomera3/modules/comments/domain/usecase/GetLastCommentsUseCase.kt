package com.numplates.nomera3.modules.comments.domain.usecase

import com.numplates.nomera3.modules.comments.data.entity.CommentsEntityResponse
import com.numplates.nomera3.modules.comments.data.repository.PostCommentsRepository
import com.numplates.nomera3.modules.comments.domain.BaseUseCaseCoroutine
import com.numplates.nomera3.modules.comments.domain.DefParams
import javax.inject.Inject

private const val LIMIT = 50L


class GetLastCommentsUseCase @Inject constructor(
        private val repository: PostCommentsRepository
) : BaseUseCaseCoroutine<GetLastCommentParams, CommentsEntityResponse?> {

    override suspend fun execute(params: GetLastCommentParams,
                                 success: (CommentsEntityResponse?) -> Unit,
                                 fail: (Exception) -> Unit) {
        repository.requestLastComments(params.postId, LIMIT, fail, success)
    }

}

class GetLastCommentParams(
        val postId: Long,
        val limit: Long = LIMIT
) : DefParams()
