package com.numplates.nomera3.modules.moments.show.domain

import com.numplates.nomera3.modules.moments.show.data.MomentsRepository
import javax.inject.Inject

class ComplainMomentUseCase @Inject constructor(
    private val momentsRepository: MomentsRepository
) {
    suspend fun invoke(
        remoteUserId: Long,
        reasonId: Int,
        momentId: Long
    ) {
        momentsRepository.momentComplain(
            remoteUserId = remoteUserId,
            reasonId = reasonId,
            momentId = momentId
        )
    }
}

class ComplainMomentParams(
    val userId: Long,
    val reasonId: Int,
    val momentId: Long
)
