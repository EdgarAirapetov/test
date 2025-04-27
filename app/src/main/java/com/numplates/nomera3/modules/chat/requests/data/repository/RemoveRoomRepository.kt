package com.numplates.nomera3.modules.chat.requests.data.repository

interface RemoveRoomRepository {
    suspend fun removeRoom(roomId: Long, shouldRemoveForBoth: Boolean)
}
