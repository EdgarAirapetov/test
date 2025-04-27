package com.numplates.nomera3.modules.chat.requests.data.repository

import com.meera.core.network.websocket.WebSocketMainChannel
import com.meera.db.DataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class RemoveRoomRepositoryImpl @Inject constructor(
    private val socket: WebSocketMainChannel,
    private val dataStore: DataStore
) : RemoveRoomRepository {

    override suspend fun removeRoom(roomId: Long, shouldRemoveForBoth: Boolean) {
        withContext(Dispatchers.IO) {
            val payload = hashMapOf(
                "room_id" to roomId,
                "both" to shouldRemoveForBoth
            )
            socket.pushDeleteRoomSuspend(payload)
            dataStore.dialogDao().updateUnreadMessageCount(roomId, 0)
            dataStore.dialogDao().deleteById(roomId)
            dataStore.draftsDao().deleteDraft(roomId)
            dataStore.messageDao().deleteMessagesByRoomId(roomId)
        }
    }
}
