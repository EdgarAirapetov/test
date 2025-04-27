package com.numplates.nomera3.modules.chat

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.google.gson.Gson
import com.meera.core.extensions.empty
import com.meera.db.DataStore
import com.meera.db.models.message.EditMessageDataDbModel
import com.meera.db.models.message.MessageEntity
import com.meera.db.models.message.SendMessageDataDbModel
import com.numplates.nomera3.data.newmessenger.ROOM_TYPE_DIALOG
import com.numplates.nomera3.modules.chat.helpers.editmessage.EDIT_MESSAGE_WORKER_INPUT_DATA_ID
import com.numplates.nomera3.modules.chat.helpers.editmessage.EditMessageWorker
import com.numplates.nomera3.modules.chat.helpers.editmessage.models.EditMessageModel
import com.numplates.nomera3.modules.chat.helpers.resendmessage.MESSAGE_ID_RESEND_PARAM
import com.numplates.nomera3.modules.chat.helpers.resendmessage.ROOM_ID_RESEND_PARAM
import com.numplates.nomera3.modules.chat.helpers.resendmessage.ResendMessageWorker
import com.numplates.nomera3.modules.chat.helpers.resendmessage.ResendType
import com.numplates.nomera3.modules.chat.helpers.resendmessage.TYPE_RESEND_PARAM
import com.numplates.nomera3.modules.chat.helpers.resendmessage.WORK_BACKOFF_DELAY
import com.numplates.nomera3.modules.chat.helpers.resendmessage.WORK_BACKOFF_TIME_UNIT
import com.numplates.nomera3.modules.chat.helpers.sendmessage.SEND_MESSAGE_WORKER_INPUT_DATA_ID
import com.numplates.nomera3.modules.chat.helpers.sendmessage.SendMessageWorker
import com.numplates.nomera3.modules.chat.helpers.sendmessage.models.ImageMessageDataModel
import com.numplates.nomera3.modules.chat.helpers.sendmessage.models.SendMessageModel
import com.numplates.nomera3.modules.chat.helpers.sendmessage.models.SendMessageType
import com.numplates.nomera3.modules.chat.helpers.sendmessage.models.VideoMessageDataModel
import com.numplates.nomera3.modules.chat.helpers.sendmessage.models.VoiceMessageDataModel
import com.numplates.nomera3.modules.chat.mediakeyboard.favorites.ui.entity.MediakeyboardFavoriteRecentUiModel
import com.numplates.nomera3.modules.chat.mediakeyboard.ui.entity.MediaPreviewType
import com.numplates.nomera3.modules.chat.workers.ReadChatMessageWorker
import com.numplates.nomera3.modules.chat.workers.ReadChatMessageWorker.Companion.MESSAGE_ID_PARAM
import com.numplates.nomera3.modules.chat.workers.ReadChatMessageWorker.Companion.ROOM_ID_PARAM
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ChatWorkManagerDelegate @Inject constructor(
    private val context: Context,
    private val gson: Gson,
    private val dataStore: DataStore,
) {

    fun resendMessages(type: ResendType): LiveData<WorkInfo> {
        var resendTypeKey = ResendType.BY_MESSAGE_ID_KEY
        var dataMessageId = String.empty()
        var dataRoomId = 0L

        when (type) {
            is ResendType.ResendByMessageId -> {
                resendTypeKey = ResendType.BY_MESSAGE_ID_KEY
                dataRoomId = type.roomId
                dataMessageId = type.messageId
            }
            is ResendType.ResendByRoomId -> {
                resendTypeKey = ResendType.BY_ROOM_ID_KEY
                dataRoomId = type.roomId
            }
        }

        val inputData = Data.Builder()
            .putInt(TYPE_RESEND_PARAM, resendTypeKey)
            .putString(MESSAGE_ID_RESEND_PARAM, dataMessageId)
            .putLong(ROOM_ID_RESEND_PARAM, dataRoomId)
            .build()

        val resendMessageRequest = OneTimeWorkRequestBuilder<ResendMessageWorker>()
            .setInputData(inputData)
            .setBackoffCriteria(
                BackoffPolicy.LINEAR, WORK_BACKOFF_DELAY, WORK_BACKOFF_TIME_UNIT
            )
            .build()

        WorkManager.getInstance(context).enqueue(resendMessageRequest)

        return WorkManager.getInstance(context)
            .getWorkInfoByIdLiveData(resendMessageRequest.id)
    }


    /**
     * Do work send read message
     * isDelayed - delay before do work
     * delayDurationMinutes - time in minutes delay before isDelay do work enabled
     */
    fun sendReadMessageDoWork(
        roomId: Long,
        message: MessageEntity,
        isDelayed: Boolean = false,
        delayDurationMinutes: Long = 10L
    ) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val inputData = Data.Builder()
            .putLong(ROOM_ID_PARAM, roomId)
            .putString(MESSAGE_ID_PARAM, message.msgId)
            .build()

        val readMessageRequest = OneTimeWorkRequestBuilder<ReadChatMessageWorker>()
            .setConstraints(constraints)
            .setInputData(inputData)

        if (isDelayed) {
            readMessageRequest.setInitialDelay(delayDurationMinutes, TimeUnit.MINUTES)
        }

        WorkManager.getInstance(context).enqueue(readMessageRequest.build())
    }

    /**
     * Do work send read message
     * isDelayed - delay before do work
     * delayDurationMinutes - time in minutes delay before isDelay do work enabled
     */
    fun sendReadMessageDoWork(
        roomId: Long,
        messageId: String,
        isDelayed: Boolean = false,
        delayDurationMinutes: Long = 10L
    ) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val inputData = Data.Builder()
            .putLong(ROOM_ID_PARAM, roomId)
            .putString(MESSAGE_ID_PARAM, messageId)
            .build()

        val readMessageRequest = OneTimeWorkRequestBuilder<ReadChatMessageWorker>()
            .setConstraints(constraints)
            .setInputData(inputData)

        if (isDelayed) {
            readMessageRequest.setInitialDelay(delayDurationMinutes, TimeUnit.MINUTES)
        }

        WorkManager.getInstance(context).enqueue(readMessageRequest.build())
    }

    suspend fun sendMessage(
        roomId: Long? = null,
        message: String? = null,
        parentMessage: MessageEntity?,
        images: List<Uri>? = null,
        audioPath: String? = null,
        videoPath: Uri? = null,
        listOfAmplitudes: List<Int>? = null,
        durationSec: Long? = null,
        gifAspectRatio: Double? = null,
        currentScrollPosition: Int = 0,
        roomType: String?,
        userId: Long?,
        sendType: SendMessageType,
        favoriteRecent: MediakeyboardFavoriteRecentUiModel? = null,
        favoriteRecentType: MediaPreviewType? = null,
    ): UUID {
        val sendData = SendMessageModel(
            roomId = roomId,
            sendType = sendType,
            userId = userId,
            messageText = message,
            parentMessage = parentMessage,
            imageData = getImageMessageModel(
                sendType = sendType,
                images = images,
                gifAspectRatio = gifAspectRatio,
            ),
            voiceData = getVoiceMessageModel(
                sendType = sendType,
                audioPath = audioPath,
                listOfAmplitudes = listOfAmplitudes,
                durationSec = durationSec
            ),
            videoData = VideoMessageDataModel(
                videoPath = videoPath?.toString()
            ),
            roomType = roomType ?: ROOM_TYPE_DIALOG,
            currentScrollPosition = currentScrollPosition,
            favoriteRecent = favoriteRecent,
            favoriteRecentType = favoriteRecentType
        )
        return sendMessageBackground(sendData)
    }

    suspend fun editMessage(editedMessage: EditMessageModel): UUID {
        return editMessageBackground(editedMessage)
    }

    private fun getVoiceMessageModel(
        sendType: SendMessageType,
        audioPath: String?,
        listOfAmplitudes: List<Int>?,
        durationSec: Long?
    ): VoiceMessageDataModel? {
        return when (sendType) {
            SendMessageType.VOICE_MESSAGE_ROOM_ID,
            SendMessageType.VOICE_MESSAGE_USER_ID -> {
                VoiceMessageDataModel(
                    audioPath = audioPath,
                    amplitudes = listOfAmplitudes,
                    durationSec = durationSec
                )
            }
            else -> null
        }
    }

    private fun getImageMessageModel(
        sendType: SendMessageType,
        images: List<Uri>?,
        gifAspectRatio: Double?,
    ): ImageMessageDataModel? {
        return when (sendType) {
            SendMessageType.SIMPLE_MESSAGE_USER_ID,
            SendMessageType.SIMPLE_MESSAGE_ROOM_ID -> {
                ImageMessageDataModel(
                    images = images?.map { uri -> uri.toString() },
                    gifAspectRatio = gifAspectRatio,
                )
            }
            else -> null
        }
    }

    private suspend fun sendMessageBackground(sendData: SendMessageModel): UUID {
        val jsonPayload = gson.toJson(sendData)
        val dbModel = SendMessageDataDbModel(dataAsJson = jsonPayload)
        val dataKey = dataStore.sendMessageDataDao().insert(dbModel)
        val inputData = Data.Builder()
            .putLong(SEND_MESSAGE_WORKER_INPUT_DATA_ID, dataKey)
            .build()

        val sendMessageRequest = OneTimeWorkRequestBuilder<SendMessageWorker>()
            .setInputData(inputData)
            .build()

        WorkManager.getInstance(context).enqueue(sendMessageRequest)

        return sendMessageRequest.id
    }

    private suspend fun editMessageBackground(sendData: EditMessageModel): UUID {
        val jsonPayload = gson.toJson(sendData)
        val dbModel = EditMessageDataDbModel(dataAsJson = jsonPayload)
        val dataKey = dataStore.editMessageDataDao().insert(dbModel)
        val inputData = Data.Builder()
            .putLong(EDIT_MESSAGE_WORKER_INPUT_DATA_ID, dataKey)
            .build()

        val editMessageRequest = OneTimeWorkRequestBuilder<EditMessageWorker>()
            .setInputData(inputData)
            .build()

        WorkManager.getInstance(context).enqueue(editMessageRequest)

        return editMessageRequest.id
    }
}
