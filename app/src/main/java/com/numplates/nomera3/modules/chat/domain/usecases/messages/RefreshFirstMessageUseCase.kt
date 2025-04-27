package com.numplates.nomera3.modules.chat.domain.usecases.messages

import com.numplates.nomera3.modules.chat.domain.ChatPersistDbRepository
import javax.inject.Inject

class RefreshFirstMessageUseCase @Inject constructor(
    private val repository: ChatPersistDbRepository
) {

    suspend fun invoke(roomId: Long): Int {
        return repository.refreshFirstMessage(roomId)
    }

}
