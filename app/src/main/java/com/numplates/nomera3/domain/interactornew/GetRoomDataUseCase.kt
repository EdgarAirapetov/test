package com.numplates.nomera3.domain.interactornew


import com.meera.db.models.dialog.DialogEntity
import com.numplates.nomera3.modules.chatrooms.data.repository.RoomDataRepository
import javax.inject.Inject

class GetRoomDataUseCase @Inject constructor(private val repository: RoomDataRepository) {

    suspend fun invoke(roomId: Long): DialogEntity? {
        return repository.getRoomDataById(roomId)
    }
}
