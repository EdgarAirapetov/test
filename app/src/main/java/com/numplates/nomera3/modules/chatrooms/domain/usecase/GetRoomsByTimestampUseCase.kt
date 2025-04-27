package com.numplates.nomera3.modules.chatrooms.domain.usecase

import com.meera.db.models.dialog.DialogEntity
import com.numplates.nomera3.modules.chatrooms.data.repository.RoomDataRepository
import javax.inject.Inject

class GetRoomsByTimestampUseCase @Inject constructor(
    private val repository: RoomDataRepository
) {

    suspend fun invoke(
        updatedAt: Long?,
        topTs: Long?,
        limit: Int
    ): List<DialogEntity> {
        return repository.getRooms(
            updatedAt = updatedAt,
            topTs = topTs,
            limit = limit,
        )
    }

}
