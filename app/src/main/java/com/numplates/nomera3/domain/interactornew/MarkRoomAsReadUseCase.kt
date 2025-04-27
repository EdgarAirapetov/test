package com.numplates.nomera3.domain.interactornew

import com.numplates.nomera3.data.network.ApiMain

class MarkRoomAsReadUseCase(private val api: ApiMain){

    suspend fun markRoomAsRead(roomIds: List<Long>) = api.markRoomAsRead(roomIds)

}