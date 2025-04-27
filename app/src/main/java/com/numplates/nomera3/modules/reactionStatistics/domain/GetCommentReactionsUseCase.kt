package com.numplates.nomera3.modules.reactionStatistics.domain

import com.numplates.nomera3.modules.reactionStatistics.data.repository.ReactionsRepository
import javax.inject.Inject

class GetCommentReactionsUseCase @Inject constructor(private val repository: ReactionsRepository) {
    suspend fun invoke(
        commentId: Long,
        reaction: String = "all",
        limit: Int = 1,
        offset: Int = 0
    ) = repository.getCommentReactions(commentId, reaction, limit, offset)

}
