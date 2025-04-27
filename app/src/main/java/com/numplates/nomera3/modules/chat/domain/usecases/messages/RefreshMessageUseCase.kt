package com.numplates.nomera3.modules.chat.domain.usecases.messages

import com.numplates.nomera3.modules.chat.domain.ChatPersistDbRepository
import javax.inject.Inject

class RefreshMessageUseCase @Inject constructor(
    private val repository: ChatPersistDbRepository
) {

    suspend fun invoke(roomId: Long, messageId: String): Int {
        return repository.refreshMessage(roomId, messageId)
    }

    suspend fun invoke(messageId: String): Int {
        return repository.refreshMessage(messageId)
    }

}
