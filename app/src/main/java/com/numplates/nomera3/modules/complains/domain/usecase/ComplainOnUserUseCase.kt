package com.numplates.nomera3.modules.complains.domain.usecase

import com.numplates.nomera3.modules.complains.data.model.UserComplaintParams
import com.numplates.nomera3.modules.complains.domain.repository.ComplaintRepository
import javax.inject.Inject

class ComplainOnUserUseCase @Inject constructor(
    private val repository: ComplaintRepository
) {
    suspend fun invoke(
        userId: Int,
        reasonId: Int,
        withFile: Boolean,
        comment: String?,
        roomId: Long?,
        momentId: Long? = null
    ): Int {
        return repository.complainOnUser(
            UserComplaintParams(
                userId = userId,
                reasonId = reasonId,
                withFile = if (withFile) 1 else 0,
                comment = comment,
                roomId = roomId,
                momentId = momentId
            )
        )
    }
}
