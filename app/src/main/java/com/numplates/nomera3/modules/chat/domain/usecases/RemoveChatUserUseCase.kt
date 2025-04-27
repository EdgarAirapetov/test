package com.numplates.nomera3.modules.chat.domain.usecases

import com.numplates.nomera3.modules.chat.domain.ChatRepository
import javax.inject.Inject

class RemoveChatUserUseCase @Inject constructor(
    private val repository: ChatRepository
) {

    suspend fun invoke(roomId: Long, userId: Long) {
        repository.removeChatUser(roomId, userId)
    }

}
