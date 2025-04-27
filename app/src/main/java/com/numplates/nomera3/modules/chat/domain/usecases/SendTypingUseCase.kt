package com.numplates.nomera3.modules.chat.domain.usecases

import com.numplates.nomera3.modules.chat.domain.ChatMessageRepository
import javax.inject.Inject

class SendTypingUseCase @Inject constructor(
    private val repository: ChatMessageRepository
) {

    suspend fun invoke(roomId: Long, type: String): Boolean {
        return repository.sendTyping(roomId, type)
    }

}
