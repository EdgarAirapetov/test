package com.numplates.nomera3.modules.complains.domain.usecase

import com.numplates.nomera3.modules.complains.data.model.MomentComplaintParams
import com.numplates.nomera3.modules.complains.domain.repository.ComplaintRepository
import javax.inject.Inject

class ComplainOnMomentUseCase @Inject constructor(
    private val repository: ComplaintRepository
) {
    suspend fun invoke(momentId: Long): Int {
        return repository.complainOnMoment(
            MomentComplaintParams(
                momentId = momentId
            )
        )
    }
}
