package com.numplates.nomera3.modules.chat.domain.usecases.messages.voice

import com.numplates.nomera3.modules.chat.domain.ChatPersistDbRepository
import javax.inject.Inject

class UpdateVoiceMessageAsStoppedUseCase @Inject constructor(
    private val repository: ChatPersistDbRepository
) {

    suspend fun invoke(roomId: Long): Int {
        return repository.updateVoiceMessageAsStopped(roomId)
    }

}
