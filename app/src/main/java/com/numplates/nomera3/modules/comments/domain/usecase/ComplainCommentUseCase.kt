package com.numplates.nomera3.modules.comments.domain.usecase

import com.numplates.nomera3.modules.comments.data.repository.PostCommentsRepository
import com.numplates.nomera3.modules.comments.domain.BaseUseCase
import com.numplates.nomera3.modules.comments.domain.DefParams
import javax.inject.Inject

class ComplainCommentUseCase @Inject constructor(
        private val repository: PostCommentsRepository
) : BaseUseCase<ComplainCommentParams, Boolean> {

    override suspend fun execute(params: ComplainCommentParams): Boolean =
            repository.commentComplain(params.commentId)
}

class ComplaintMomentCommentUseCase @Inject constructor(
    private val repository: PostCommentsRepository
) : BaseUseCase<ComplainCommentParams, Boolean> {

    override suspend fun execute(params: ComplainCommentParams): Boolean =
        repository.momentCommentComplain(params.commentId)
}

class ComplainCommentParams(
        val commentId: Long
) : DefParams()
