package com.numplates.nomera3.modules.moments.show.domain

import com.numplates.nomera3.modules.moments.show.data.MomentsRepository
import javax.inject.Inject

class AllowCommentsUseCase @Inject constructor(
    private val momentsRepository: MomentsRepository
) {
    suspend fun invoke(
        momentId: Long,
        commentAvailability: CommentsAvailabilityType
    ) {
        momentsRepository.setCommentAvailability(
            momentId = momentId,
            commentAvailability = commentAvailability.index
        )
    }
}
