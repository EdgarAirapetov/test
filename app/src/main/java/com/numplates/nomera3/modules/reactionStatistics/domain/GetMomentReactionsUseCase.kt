package com.numplates.nomera3.modules.reactionStatistics.domain

import com.numplates.nomera3.modules.reactionStatistics.data.repository.ReactionsRepository
import javax.inject.Inject

class GetMomentReactionsUseCase @Inject constructor(private val repository: ReactionsRepository) {
    suspend fun invoke(
        momentId: Long,
        reaction: String,
        limit: Int,
        offset: Int
    ) = repository.getMomentReactions(momentId, reaction, limit, offset)

}
