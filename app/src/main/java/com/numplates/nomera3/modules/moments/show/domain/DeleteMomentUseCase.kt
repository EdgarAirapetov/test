package com.numplates.nomera3.modules.moments.show.domain

import com.numplates.nomera3.modules.moments.show.data.MomentsRepository
import javax.inject.Inject

class DeleteMomentUseCase @Inject constructor(
    private val momentsRepository: MomentsRepository
) {
    suspend fun invoke(momentId: Long, userId: Long) = momentsRepository.deleteMoment(momentId = momentId, userId = userId)
}
