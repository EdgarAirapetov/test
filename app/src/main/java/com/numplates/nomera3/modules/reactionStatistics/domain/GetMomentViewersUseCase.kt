package com.numplates.nomera3.modules.reactionStatistics.domain

import com.numplates.nomera3.modules.reactionStatistics.data.repository.ReactionsRepository
import javax.inject.Inject

class GetMomentViewersUseCase @Inject constructor(private val repository: ReactionsRepository) {
    suspend fun invoke(
        momentId: Long,
        limit: Int,
        offset: Int
    ) = repository.getMomentViewers(momentId, limit, offset)
}
