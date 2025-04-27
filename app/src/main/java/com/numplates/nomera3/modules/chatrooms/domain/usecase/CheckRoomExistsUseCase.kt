package com.numplates.nomera3.modules.chatrooms.domain.usecase

import com.numplates.nomera3.modules.chatrooms.data.repository.RoomDataRepository
import javax.inject.Inject

class CheckRoomExistsUseCase @Inject constructor(
    private val repository: RoomDataRepository
) {

    /**
     * @return Pair(isRoomExists, roomId)
     */
    suspend fun invoke(userId: Long, roomType: String): Pair<Boolean, Long> {
        return repository.checkRoomOnServer(userId, roomType)
    }

}
