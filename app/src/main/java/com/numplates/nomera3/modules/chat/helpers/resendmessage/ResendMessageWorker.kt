package com.numplates.nomera3.modules.chat.helpers.resendmessage

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.meera.core.extensions.empty
import com.meera.db.DataStore
import com.meera.db.models.message.MessageAttachment
import com.meera.db.models.message.MessageEntity
import com.numplates.nomera3.App
import com.numplates.nomera3.modules.chat.messages.presentation.ITEM_TYPE_NO_MEDIA_PLACEHOLDER_SEND
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

const val TYPE_RESEND_PARAM = "TYPE_RESEND_PARAM"
const val MESSAGE_ID_RESEND_PARAM = "MESSAGE_ID_RESEND_PARAM"
const val ROOM_ID_RESEND_PARAM = "ROOM_ID_RESEND_PARAM"
const val RESEND_MAX_ATTEMPT_COUNT = 3

// Интервал повторения задачи по ресенду
const val WORK_BACKOFF_DELAY = 20L
val WORK_BACKOFF_TIME_UNIT = TimeUnit.SECONDS

/**
 * Воркер для ресенда всех типов сообщений в фоне
 */
class ResendMessageWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams), IResendResultCallback {

    @Inject
    lateinit var resendService: ResendMessageService

    @Inject
    lateinit var dataStore: DataStore

    @Inject
    lateinit var notificationUtil: ResendNotificationUtil

    init {
        App.component.inject(this)
        resendService.addResendResultCallback(this)
    }

    override suspend fun doWork(): Result {
        val resendTypeKey = inputData.getInt(TYPE_RESEND_PARAM, ResendType.BY_MESSAGE_ID_KEY)
        val messageId = inputData.getString(MESSAGE_ID_RESEND_PARAM) ?: String.empty()
        val roomId = inputData.getLong(ROOM_ID_RESEND_PARAM, 0)

        if (runAttemptCount > RESEND_MAX_ATTEMPT_COUNT) {
            hideAllResendProgress(roomId)
            return Result.failure()
        }

        try {
            val type = resendTypeResolver(resendTypeKey, messageId, roomId)
            resendService.resendMessage(type)
        } catch (e: Exception) {
            Timber.e(e)
            return Result.retry()
        }

        return Result.success()
    }

    /**
     * Определяем, будем ресендить все сообщения по roomId
     * или одно единственное сообщение
     */
    private fun resendTypeResolver(
        resendTypeKey: Int,
        messageId: String,
        roomId: Long
    ): ResendType {
        if (resendTypeKey == ResendType.BY_MESSAGE_ID_KEY) {
            return ResendType.ResendByMessageId(messageId, roomId)
        } else if (resendTypeKey == ResendType.BY_ROOM_ID_KEY) {
            return ResendType.ResendByRoomId(roomId)
        }
        return ResendType.ResendByMessageId(messageId, roomId)
    }

    override fun onProgressResend(message: MessageEntity) {
        updateMessageSentStatus(message, isSent = false, isShowResendProgress = true)
    }

    override fun onSuccessResend(message: MessageEntity) {
        updateMessageSentStatus(message, isSent = true, isShowResendProgress = false)
    }

    override fun onFailResend(message: MessageEntity) {
        // do nothing
    }

    override fun onSetMediaPlaceholder(message: MessageEntity, notExistPaths: List<String?>) {
        // Для удаленных нескольких изображений поставить Placeholder
        val attachments = message.attachments
        if (attachments.isNotEmpty()) {
            attachments.forEach { attach ->
                if(notExistPaths.contains(attach.url)){
                    attach.url = MessageAttachment.EMPTY_URL
                }
            }
        } else {
            // Single image
            message.itemType = ITEM_TYPE_NO_MEDIA_PLACEHOLDER_SEND
            message.attachment.url = MessageAttachment.EMPTY_URL
        }

        dataStore.messageDao().update(message)
    }

    override fun onDisableResend(message: MessageEntity) {
        updateMessageSentStatus(
            message,
            isSent = false,
            isShowResendProgress = false,
            isResendAvailable = false
        )
    }

    private fun updateMessageSentStatus(
        message: MessageEntity,
        isSent: Boolean,
        isShowResendProgress: Boolean,
        isResendAvailable: Boolean = true
    ) {
        message.sent = isSent
        message.isResendProgress = isShowResendProgress
        message.isResendAvailable = isResendAvailable
        dataStore.messageDao().update(message)
    }

    private fun hideAllResendProgress(roomId: Long) {
        dataStore.messageDao().updateAllResendProgressStatusByRoomId(
            roomId = roomId,
            showResendProgress = false
        )
    }

}
