package com.numplates.nomera3.modules.chat.helpers.editmessage

import android.net.Uri
import android.webkit.URLUtil
import com.meera.db.models.message.MessageAttachment
import com.numplates.nomera3.domain.interactornew.GetUserUidUseCase
import com.numplates.nomera3.modules.chat.domain.usecases.GetMessageByIdUseCase
import com.numplates.nomera3.modules.chat.domain.usecases.UpdateChatMessageUsecase
import com.numplates.nomera3.modules.chat.helpers.UploadChatHelper
import com.numplates.nomera3.modules.chat.helpers.editmessage.models.EditMessageModel
import com.numplates.nomera3.modules.chat.helpers.resolveMessageType
import com.numplates.nomera3.modules.chat.messages.data.mapper.EditMessageMapper
import com.numplates.nomera3.modules.chat.messages.domain.mapper.MessageAttachmentModelMapper
import com.numplates.nomera3.modules.chat.messages.domain.model.MessageAttachmentModel
import com.numplates.nomera3.modules.chat.messages.domain.usecase.EditMessageUseCase
import kotlinx.coroutines.delay
import javax.inject.Inject

// minimal time for execution to prevent loader blinking
private const val WORKING_PROGRESS_DELAY = 300L

class EditMessageManager @Inject constructor(
    private val uploadHelper: UploadChatHelper,
    private val editMessageUseCase: EditMessageUseCase,
    private val updateChatMessageUseCase: UpdateChatMessageUsecase,
    private val getMessageByIdUseCase: GetMessageByIdUseCase,
    private val messageAttachmentModelMapper: MessageAttachmentModelMapper,
    private val getUserUidUseCase: GetUserUidUseCase,
    private val mapper: EditMessageMapper,
) {

    private var actionCallback: EditMessageInteractionCallback? = null

    suspend fun editMessage(editedMessage: EditMessageModel) {
        val mediaToUpload = editedMessage.attachments?.filterNot { URLUtil.isNetworkUrl(it.uriPath) }.orEmpty()
        val alreadyUploadedMedia = editedMessage.attachments?.filter { URLUtil.isNetworkUrl(it.uriPath) }.orEmpty()
        val recentlyUploadedMedia = uploadMediasIfRequired(
            messageId = editedMessage.messageId,
            attachments = mediaToUpload
        )
        val preparedMessage = editedMessage.copy(attachments = alreadyUploadedMedia.plus(recentlyUploadedMedia))
        editMessageUseCase.invoke(preparedMessage)
        val local = getMessageByIdUseCase.invoke(editedMessage.messageId)
        if (local != null) {
            val updatedText = preparedMessage.messageText.toString()
            val updatedTagSpan = local.tagSpan?.copy(text = updatedText)
            val attachmentDtos = preparedMessage.attachments?.map(mapper::mapEditMessageAttachment)
            val attachment: MessageAttachment? = attachmentDtos?.takeIf { it.size == 1 }?.firstOrNull()
            val attachments: List<MessageAttachment> = attachmentDtos?.takeIf { it.size > 1 } ?: emptyList()
            val itemType = resolveMessageType(
                creatorId = local.creator?.userId,
                attachment = attachment,
                attachments = attachments,
                deleted = local.deleted,
                eventCode = local.eventCode,
                type = local.type,
                myUid = getUserUidUseCase.invoke()
            )
            val updated = local.copy(
                itemType = itemType,
                content = updatedText,
                tagSpan = updatedTagSpan,
                editedAt = System.currentTimeMillis(),
            )
            updateChatMessageUseCase.invoke(updated)
        }
    }

    fun addInteractionCallback(callback: EditMessageInteractionCallback) {
        this.actionCallback = callback
    }

    private suspend fun uploadMediasIfRequired(
        messageId: String,
        attachments: List<MessageAttachmentModel>
    ): List<MessageAttachmentModel> {
        return if (attachments.isNotEmpty()) {
            actionCallback?.onShowLoadingProgress(messageId)
            val uploadedMedias = uploadHelper.uploadChatMedia(
                mediaToUpload = attachments.map { Uri.parse(it.uriPath) }
            ).map(messageAttachmentModelMapper::map)
            delay(WORKING_PROGRESS_DELAY)
            actionCallback?.onHideLoadingProgress(messageId)
            uploadedMedias
        } else {
            emptyList()
        }
    }
}
