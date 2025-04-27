package com.numplates.nomera3.modules.chatrooms.domain.usecase

import com.numplates.nomera3.modules.chatrooms.data.repository.RoomDataRepository
import javax.inject.Inject

class GetRoomsMaxUpdatedAtUseCase @Inject constructor(
    private val repository: RoomDataRepository
) {

    suspend fun invoke(): Long = repository.getRoomsMaxUpdatedAt()

}
