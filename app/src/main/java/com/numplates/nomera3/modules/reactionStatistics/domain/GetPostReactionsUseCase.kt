package com.numplates.nomera3.modules.reactionStatistics.domain

import com.numplates.nomera3.modules.reactionStatistics.data.repository.ReactionsRepository
import javax.inject.Inject

class GetPostReactionsUseCase @Inject constructor(private val repository: ReactionsRepository) {
    suspend fun invoke(
        postId: Long,
        reaction: String,
        limit: Int,
        offset: Int
    ) = repository.getPostReactions(postId, reaction, limit, offset)

}
