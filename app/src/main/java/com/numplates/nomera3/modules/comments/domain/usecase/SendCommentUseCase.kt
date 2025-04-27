package com.numplates.nomera3.modules.comments.domain.usecase

import com.numplates.nomera3.modules.comments.data.entity.SendCommentResponse
import com.numplates.nomera3.modules.comments.data.repository.PostCommentsRepository
import com.numplates.nomera3.modules.comments.data.repository.SendCommentError
import com.numplates.nomera3.modules.comments.domain.BaseUseCase
import com.numplates.nomera3.modules.comments.domain.DefParams
import javax.inject.Inject

class SendCommentUseCase @Inject constructor(
        private val repository: PostCommentsRepository
): BaseUseCase<SendCommentParams, SendCommentResponse?> {

    override suspend fun execute(params: SendCommentParams)
            = repository.sendComment(params.postId, params.text, params.commentId, params.errorTypeListener)

}

class SendCommentParams(
        val postId: Long,
        val text: String,
        val commentId: Long,
        val errorTypeListener: (SendCommentError) -> Unit
) : DefParams()
