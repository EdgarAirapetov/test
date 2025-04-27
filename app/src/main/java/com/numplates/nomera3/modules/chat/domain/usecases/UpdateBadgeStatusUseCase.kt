package com.numplates.nomera3.modules.chat.domain.usecases

import com.numplates.nomera3.modules.chat.data.repository.ChatMessageRepositoryImpl
import javax.inject.Inject

class UpdateBadgeStatusUseCase @Inject constructor(
    private val repository: ChatMessageRepositoryImpl
) {

    suspend fun invoke(
        roomId: Long?,
        needToShowBadge: Boolean
    ): Int {
        return repository.updateBadgeStatus(
            roomId = roomId ?: 0L,
            needToShowBadge = needToShowBadge
        )
    }

}
