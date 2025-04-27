package com.numplates.nomera3.modules.reactionStatistics.domain

import com.numplates.nomera3.modules.reactionStatistics.data.repository.ReactionsRepository
import javax.inject.Inject

class GetMomentCommentReactionsUseCase @Inject constructor(private val repository: ReactionsRepository) {
    suspend fun invoke(
        commentId: Long,
        reaction: String,
        limit: Int,
        offset: Int
    ) = repository.getMomentCommentReactions(commentId, reaction, limit, offset)

}
