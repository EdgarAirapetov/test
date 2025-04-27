package com.numplates.nomera3.modules.chat.requests.domain.usecase

import com.numplates.nomera3.modules.chat.requests.data.repository.RemoveRoomRepository
import javax.inject.Inject

class RemoveRoomByIdUseCase @Inject constructor(
    private val repository: RemoveRoomRepository
) {

    suspend fun invoke(roomId: Long, shouldRemoveForBoth: Boolean) {
        repository.removeRoom(roomId, shouldRemoveForBoth)
    }
}
