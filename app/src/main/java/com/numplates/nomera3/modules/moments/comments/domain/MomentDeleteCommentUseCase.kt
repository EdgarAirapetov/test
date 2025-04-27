package com.numplates.nomera3.modules.moments.comments.domain

import com.numplates.nomera3.modules.moments.core.MomentsApi
import javax.inject.Inject

class MomentDeleteCommentUseCase @Inject constructor(private val api: MomentsApi) {
    suspend fun invoke(commentId: Long) {
        api.deleteComment(commentId)
    }
}
