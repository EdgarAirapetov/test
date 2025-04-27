package com.numplates.nomera3.modules.chatrooms.domain.usecase

import com.numplates.nomera3.modules.chatrooms.data.api.GetRoomsResponse
import com.numplates.nomera3.modules.chatrooms.data.repository.RoomDataRepository
import javax.inject.Inject

class GetRoomsResponseUseCase @Inject constructor(
    private val repository: RoomDataRepository
) {

    suspend fun invoke(
        updatedAt: Long?,
        topTs: Long?,
        limit: Int
    ): GetRoomsResponse? {
        return repository.getRoomsResponse(
            updatedAt = updatedAt,
            topTs = topTs,
            limit = limit
        )
    }

}
