package com.numplates.nomera3.modules.chat.domain.usecases.messages

import com.numplates.nomera3.modules.chat.domain.ChatPersistDbRepository
import javax.inject.Inject

class ReadAndDecrementMessageUseCase @Inject constructor(
    private val repository: ChatPersistDbRepository
) {

    suspend fun invoke(roomId: Long, messageId: String) {
        return repository.readAndDecrementMessageUseCase(roomId, messageId)
    }

}
