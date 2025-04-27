package com.numplates.nomera3.modules.chat.helpers.resendmessage

import android.content.Context
import com.google.gson.Gson
import com.meera.core.utils.files.FileManager
import com.meera.db.DataStore
import com.meera.db.models.message.MessageEntity
import com.numplates.nomera3.domain.interactornew.SendNewMessageUseCase
import com.numplates.nomera3.domain.interactornew.SendVoiceMessageUseCase
import com.numplates.nomera3.modules.chat.helpers.UploadChatHelper
import com.numplates.nomera3.modules.chat.messages.presentation.ITEM_TYPE_AUDIO_SEND
import com.numplates.nomera3.modules.chat.messages.presentation.ITEM_TYPE_IMAGE_SEND
import com.numplates.nomera3.modules.chat.messages.presentation.ITEM_TYPE_ONLY_TEXT_SEND
import com.numplates.nomera3.modules.chat.messages.presentation.ITEM_TYPE_SEND
import com.numplates.nomera3.modules.chat.messages.presentation.ITEM_TYPE_VIDEO_SEND
import com.numplates.nomera3.modules.fileuploads.domain.usecase.PartialUploadChatVideoUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import javax.inject.Inject


class ResendMessageService @Inject constructor(
    private val appContext: Context,
    private val dataStore: DataStore,
    private val newMessageUseCase: SendNewMessageUseCase,
    private val partialUploadChatVideoUseCase: PartialUploadChatVideoUseCase,
    private val chatUploadHelper: UploadChatHelper,
    private val sendVoiceMessageUseCase: SendVoiceMessageUseCase,
    private val gson: Gson,
    private val fileManager: FileManager
) : IResendResultCallback {

    private var resultCallback: IResendResultCallback? = null

    fun addResendResultCallback(resultCallback: IResendResultCallback) {
        this.resultCallback = resultCallback
    }

    suspend fun resendMessage(type: ResendType) {
        when (type) {
            is ResendType.ResendByMessageId -> resendSingleMessage(type.messageId)
            is ResendType.ResendByRoomId -> resendAllRoomMessages(type.roomId)
        }
    }

    private suspend fun resendSingleMessage(messageId: String) {
        val unsentMessage = findSingleUnsentMessage(messageId)
        runResendTasks(mutableListOf(unsentMessage))
    }

    private suspend fun resendAllRoomMessages(roomId: Long) {
        val unsentMessages = findAllUnsentMessages(roomId)
        runResendTasks(unsentMessages)
    }

    private suspend fun findSingleUnsentMessage(messageId: String) = withContext(Dispatchers.IO) {
        return@withContext dataStore.messageDao().getSingleUnsentMessage(messageId)
    }

    private suspend fun findAllUnsentMessages(roomId: Long) = withContext(Dispatchers.IO) {
        return@withContext dataStore.messageDao().getAllUnsentMessages(roomId)
    }

    private suspend fun runResendTasks(unsentMessages: List<MessageEntity>) = coroutineScope {
        resolveUnsentMessageType(unsentMessages).forEach { task ->
            task.execute()
        }
    }

    private fun resolveUnsentMessageType(messages: List<MessageEntity>): List<IResendTask> {
        val dependencies = TaskDependencies(
            appContext = appContext,
            newMessageUseCase = newMessageUseCase,
            dataStore = dataStore,
            partialUploadChatVideoUseCase = partialUploadChatVideoUseCase,
            chatUploadHelper = chatUploadHelper,
            sendVoiceMessageUseCase = sendVoiceMessageUseCase,
            gson = gson
        )
        return messages.map { message ->
            return@map when (message.itemType) {
                ITEM_TYPE_ONLY_TEXT_SEND ->
                    ResendTextMessageTask(dependencies, message, this, fileManager)
                ITEM_TYPE_IMAGE_SEND, ITEM_TYPE_SEND ->
                    ResendImageMessageTask(dependencies, message, this, fileManager)
                ITEM_TYPE_AUDIO_SEND ->
                    ResendVoiceMessageTask(dependencies, message, this, fileManager)
                ITEM_TYPE_VIDEO_SEND ->
                    ResendVideoMessageTask(dependencies, message, this, fileManager)
                else ->
                    ResendTextMessageTask(dependencies, message, this, fileManager)
            }
        }
    }

    override fun onProgressResend(message: MessageEntity) {
        resultCallback?.onProgressResend(message)
    }

    override fun onSuccessResend(message: MessageEntity) {
        resultCallback?.onSuccessResend(message)
    }

    override fun onFailResend(message: MessageEntity) {
        resultCallback?.onFailResend(message)
    }

    override fun onSetMediaPlaceholder(message: MessageEntity, notExistPaths: List<String?>) {
        resultCallback?.onSetMediaPlaceholder(message, notExistPaths)
    }

    override fun onDisableResend(message: MessageEntity) {
        resultCallback?.onDisableResend(message)
    }
}
