package com.numplates.nomera3.modules.chat.domain.usecases.messages

import com.numplates.nomera3.modules.chat.domain.ChatMessageRepository
import javax.inject.Inject

class UpdateLastUnreadMessageTsUseCase @Inject constructor(
    private val repository: ChatMessageRepository
) {

    suspend fun invoke(
        roomId: Long,
        timestamp: Long
    ): Int {
        return repository.updateLastUnreadMessageTs(
            roomId = roomId,
            timestamp = timestamp
        )
    }

}
