package com.numplates.nomera3.modules.chat.domain.usecases.messages

import com.numplates.nomera3.modules.chat.domain.ChatMessageRepository
import javax.inject.Inject

class ReadMessageNetworkUseCase @Inject constructor(
    private val repository: ChatMessageRepository
) {

    suspend fun invoke(roomId: Long, messageIds: List<String>): Boolean {
        return repository.readMessageNetwork(roomId, messageIds)
    }

}
