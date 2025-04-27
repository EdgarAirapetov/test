package com.numplates.nomera3.modules.chat.domain.usecases.messages

import com.numplates.nomera3.modules.chat.domain.ChatMessageRepository
import javax.inject.Inject

class DeleteMessageNetworkUseCase @Inject constructor(
    private val repository: ChatMessageRepository
) {

    suspend fun invoke(
        roomId: Long,
        messageId: String,
        isBoth: Boolean
    ): Boolean {
        return repository.deleteMessageNetwork(roomId, messageId, isBoth)
    }

}
