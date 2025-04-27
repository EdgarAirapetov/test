package com.numplates.nomera3.modules.chat.helpers.sendmessage

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.google.gson.Gson
import com.meera.db.DataStore
import com.numplates.nomera3.App
import com.numplates.nomera3.modules.chat.helpers.sendmessage.models.ActionInsertDbMessage
import com.numplates.nomera3.modules.chat.helpers.sendmessage.models.ActionSendMessageWorkResult
import com.numplates.nomera3.modules.chat.helpers.sendmessage.models.SendMessageModel
import com.numplates.nomera3.modules.chat.helpers.sendmessage.models.SendMessageWorkResultKey
import com.numplates.nomera3.modules.chat.helpers.sendmessage.models.SuccessSendMessageWorkResult
import kotlinx.coroutines.delay
import timber.log.Timber
import javax.inject.Inject

const val SEND_MESSAGE_WORKER_INPUT_DATA_ID = "send_message_input_data_id"
private const val WORKING_PROGRESS_DELAY = 300L
const val INVALID_SEND_MESSAGE_DATA_ID = -1L

class SendMessageWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams), SendMessageInteractionCallback {

    @Inject
    lateinit var sendManager: SendMessageManager

    @Inject
    lateinit var gson: Gson

    @Inject
    lateinit var dataStore: DataStore

    init {
        App.component.inject(this)
        sendManager.addInteractionCallback(this)
    }

    override suspend fun doWork(): Result {
        val dataKey = inputData.getLong(SEND_MESSAGE_WORKER_INPUT_DATA_ID, INVALID_SEND_MESSAGE_DATA_ID)
        return try {
            if (dataKey == INVALID_SEND_MESSAGE_DATA_ID) throw Exception("Invalid input data key")
            val sendData = dataStore.sendMessageDataDao().getDataByKey(dataKey)
                ?: throw Exception("Could not retrieve input data")
            val parsedData = gson.fromJson(sendData.dataAsJson, SendMessageModel::class.java)
            sendManager.sendMessage(parsedData)
            delay(WORKING_PROGRESS_DELAY)
            Result.success()
        } catch (e: Exception) {
            Timber.e(e)
            Result.failure()
        } finally {
            runCatching { dataStore.sendMessageDataDao().deleteByKey(dataKey)}
                .onFailure { Timber.e(it) }
        }
    }

    override suspend fun onSuccessSendMessage(
        roomId: Long,
        guestId: Long,
        chatType: String
    ) {
        val result = SuccessSendMessageWorkResult(
            roomId = roomId,
            guestId = guestId,
            chatType = chatType
        ).serialize(gson)

        setProgress(workDataOf(
            SendMessageWorkResultKey.SUCCESS_SEND.key to result
        ))
    }

    override suspend fun onInsertDbMessage(messageId: String) {
        val action = ActionInsertDbMessage(
            messageId = messageId,
            workId = id.toString()
        )
        val payload = gson.toJson(action)
        setProgress(workDataOf(
            SendMessageWorkResultKey.ACTION_INSERT_DB_MESSAGE.key to payload
        ))
    }

    override suspend fun onActionSendMessage(
        messageId: String,
        isSentError: Boolean,
        resultMessage: String
    ) {
        val result = ActionSendMessageWorkResult(
            messageId = messageId,
            isSentError = isSentError,
        ).serialize(gson)

        setProgress(workDataOf(
            SendMessageWorkResultKey.ACTION_SEND_MESSAGE.key to result,
        ))
    }


}
