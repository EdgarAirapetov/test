package com.numplates.nomera3.modules.moments.comments.domain

import com.numplates.nomera3.data.network.core.ResponseWrapper
import com.numplates.nomera3.modules.comments.data.entity.SendCommentResponse
import com.numplates.nomera3.modules.moments.show.data.MomentsRepository
import javax.inject.Inject

private const val COMMENT_RESTRICTED_CODE = 2
private const val COMMENT_MOMENT_NOT_FOUND_CODE = 1

class MomentCommentSendUseCase @Inject constructor(private val momentsRepository: MomentsRepository) {
    suspend fun invoke(
        momentItemId: Long,
        text: String,
        commentId: Long,
    ): ResponseWrapper<SendCommentResponse> {
        val resultData = momentsRepository.sendComment(
            momentItemId = momentItemId,
            text = text,
            commentId = commentId
        )

        checkOnErrorThrow(resultData)

        return resultData
    }

    private fun checkOnErrorThrow(resultData: ResponseWrapper<SendCommentResponse>) {
        if (resultData.err == null) {
            return
        }

        when (resultData.err?.code) {
            COMMENT_RESTRICTED_CODE, COMMENT_MOMENT_NOT_FOUND_CODE -> {
                throw CommentRestrictedException(resultData.err.userMessage ?: "")
            }
            else -> {
                throw CommentRestrictedExceptionUnknown
            }
        }
    }

    data class CommentRestrictedException(val userMessage: String) : Exception()
    object CommentRestrictedExceptionUnknown : Exception()
}
