package com.numplates.nomera3.modules.chat.requests.domain.usecase

import com.meera.db.models.dialog.DialogEntity
import com.numplates.nomera3.modules.chat.requests.data.repository.ChatRequestRepository
import javax.inject.Inject

class HideChatRequestRoomsUseCase @Inject constructor(
    private val repository: ChatRequestRepository
) {
    suspend fun invoke(room: DialogEntity) =
        repository.changeChatRequestVisibility(roomId = room.roomId, isHidden = true)

    suspend fun invoke(roomId: Long) =
        repository.changeChatRequestVisibility(roomId = roomId, isHidden = true)
}
