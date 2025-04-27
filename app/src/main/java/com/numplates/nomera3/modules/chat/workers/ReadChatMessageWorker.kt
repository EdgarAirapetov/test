package com.numplates.nomera3.modules.chat.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.meera.core.network.websocket.WebSocketMainChannel
import com.numplates.nomera3.App
import com.numplates.nomera3.data.websocket.STATUS_ERROR
import com.numplates.nomera3.data.websocket.STATUS_OK
import timber.log.Timber
import javax.inject.Inject

class ReadChatMessageWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val ROOM_ID_PARAM = "room_id"
        const val MESSAGE_ID_PARAM = "message_id"
    }

    @Inject
    lateinit var webSocketMainChannel: WebSocketMainChannel

    init {
        App.component.inject(this)
    }

    override suspend fun doWork(): Result {
        val roomId = inputData.getLong(ROOM_ID_PARAM, 0)
        val messageId = inputData.getString(MESSAGE_ID_PARAM)

        Timber.e("DO Work: send read message to server RoomsId($roomId) MessageId($messageId)")

        val payload = hashMapOf(
                "room_id" to roomId,
                "ids" to mutableListOf(messageId)
        )

        val api = webSocketMainChannel.pushMessageReadCoroutine(payload)
        when (api.payload["status"]) {
            STATUS_OK -> {
                Timber.e("OK Send message")
            }
            STATUS_ERROR -> {
                Timber.e("ERROR Send message")

            }
        }

        return Result.success()
    }
}
