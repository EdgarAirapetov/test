package com.numplates.nomera3.modules.chatrooms.domain.usecase

import com.meera.db.models.dialog.DialogEntity
import com.numplates.nomera3.modules.chatrooms.data.repository.RoomDataRepository
import javax.inject.Inject

class GetRoomUseCase @Inject constructor(
    private val repository: RoomDataRepository
) {

    suspend fun invoke(roomId: Long?): DialogEntity? {
        return repository.getRoom(roomId)
    }

}
