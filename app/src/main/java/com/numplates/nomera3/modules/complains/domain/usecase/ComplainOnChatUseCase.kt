package com.numplates.nomera3.modules.complains.domain.usecase

import com.numplates.nomera3.modules.complains.data.model.ChatComplaintParams
import com.numplates.nomera3.modules.complains.domain.repository.ComplaintRepository
import javax.inject.Inject

class ComplainOnChatUseCase @Inject constructor(
    private val repository: ComplaintRepository
) {

    suspend fun invoke(
        roomId: Long?,
        reasonId: Int,
        withFile: Boolean,
        comment: String?,
    ): Int {
        return repository.complainOnChat(
            ChatComplaintParams(
                roomId = roomId,
                reasonId = reasonId,
                withFile = if (withFile) 1 else 0,
                comment = comment
            )
        )
    }

}
