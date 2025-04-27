package com.numplates.nomera3.modules.comments.domain.usecase

import com.numplates.nomera3.modules.comments.data.repository.PostCommentsRepository
import com.numplates.nomera3.modules.comments.domain.DefParams
import javax.inject.Inject

class DeletePostCommentUseCase @Inject constructor(private val repository: PostCommentsRepository) {

    suspend fun invoke(params: DeletePostCommentParams) {
        repository.deletePostComment(params.commentId)
    }

}

class DeletePostCommentParams(val commentId: Long) : DefParams()
