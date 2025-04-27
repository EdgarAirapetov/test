package com.numplates.nomera3.modules.chatrooms.domain.usecase


import com.meera.db.models.dialog.DialogEntity
import com.numplates.nomera3.modules.chatrooms.data.repository.RoomDataRepository
import com.numplates.nomera3.modules.chatrooms.pojo.RoomTimeType
import javax.inject.Inject

class GetRoomsUseCase @Inject constructor(
    private val repository: RoomDataRepository
) {

    suspend fun invoke(
        limit: Int,
        type: RoomTimeType
    ): List<DialogEntity> {
        return repository.getRooms(
            limit = limit,
            type = type
        )
    }

}
