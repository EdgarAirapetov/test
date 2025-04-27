package com.numplates.nomera3.modules.moments.show.domain

import com.numplates.nomera3.modules.moments.show.data.MomentsRepository
import javax.inject.Inject

class GetMomentLinkUseCase @Inject constructor(
    private val repository: MomentsRepository
) {

    suspend fun invoke(
        momentId: Long
    ): MomentLinkModel {
        return repository.getMomentLink(momentId = momentId)
    }
}
