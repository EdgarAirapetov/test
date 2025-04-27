package com.numplates.nomera3.modules.chat.domain.usecases

import com.numplates.nomera3.modules.chat.domain.ChatRepository
import javax.inject.Inject

class ChangeMuteStateUseCase @Inject constructor(
    private val repository: ChatRepository
) {

    suspend fun invoke(roomId: Long, isMuted: Boolean) {
        repository.changeRoomMuteState(roomId, isMuted)
    }
}
