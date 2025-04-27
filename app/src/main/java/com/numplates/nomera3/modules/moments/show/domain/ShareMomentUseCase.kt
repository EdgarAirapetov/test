package com.numplates.nomera3.modules.moments.show.domain

import com.numplates.nomera3.modules.moments.show.data.MomentsRepository
import javax.inject.Inject

class ShareMomentUseCase @Inject constructor(
    private val repository: MomentsRepository
) {

    suspend fun invoke(
        momentId: Long,
        userIds: List<Long>,
        roomIds: List<Long>,
        comment: String
    ): MomentItemModel {
        return repository.shareMoment(
            momentId = momentId,
            userIds = userIds,
            roomIds = roomIds,
            comment = comment
        )
    }
}
