package com.numplates.nomera3.modules.chat.messages.data.repository

import com.meera.core.extensions.empty
import com.meera.db.models.message.MessageAttachment
import com.numplates.nomera3.data.network.core.ResponseWrapper
import com.numplates.nomera3.modules.chat.data.GreetingRequestBodyDto
import com.numplates.nomera3.modules.chat.messages.data.EditMessageDtos
import com.numplates.nomera3.modules.chat.messages.data.api.MessagesApi
import com.numplates.nomera3.modules.chat.messages.data.entity.ForwardMessageEntityResponse
import com.numplates.nomera3.modules.chat.messages.data.entity.SendMultipleParams
import com.numplates.nomera3.modules.chat.messages.data.mapper.EditMessageMapper
import com.numplates.nomera3.modules.chat.messages.domain.model.MessageAttachmentModel
import com.numplates.nomera3.modules.fileuploads.domain.model.ChatAttachmentPartialUploadModel
import com.numplates.nomera3.modules.userprofile.data.entity.GreetingResponse
import javax.inject.Inject

class MessagesRepositoryImpl @Inject constructor(
    private val messagesApi: MessagesApi,
    private val mapper: EditMessageMapper,
) : MessagesRepository {

    override suspend fun forwardMessage(
        messageId: String,
        roomId: Long,
        userIds: List<Long>?,
        roomIds: List<Long>?,
        comment: String,
        success: (ResponseWrapper<ForwardMessageEntityResponse>) -> Unit,
        fail: (Exception) -> Unit
    ) {
        try {
            val response = messagesApi.forwardMessage(
                messageId = messageId,
                roomId = roomId,
                userIds = userIds,
                roomIds = roomIds,
                comment = comment
            )
            if (response.data != null || response.err != null) {
                success(response)
            } else {
                fail(IllegalArgumentException("Empty response"))
            }
        } catch (e: Exception) {
            fail(e)
        }
    }

    override suspend fun sendGreeting(
        userId: Long,
        stickerId: Int?,
        success: (GreetingResponse) -> Unit,
        fail: (Exception) -> Unit
    ) {
        try {
            val body = GreetingRequestBodyDto(userId, stickerId)
            val response = messagesApi.sendGreeting(body)
            if (response.data != null) {
                success(response.data)
            } else {
                fail(IllegalArgumentException("Empty response"))
            }
        } catch (e: Exception) {
            fail(e)
        }
    }

    override suspend fun editMessage(
        messageId: String,
        roomId: Long,
        messageText: String?,
        attachments: List<MessageAttachmentModel>?
    ) {
        val attachmentDtos = attachments?.map(mapper::mapEditMessageAttachment)
        val attachment: Any = attachmentDtos?.takeIf { it.size == 1 }?.first() ?: String.empty()
        val otherAttachments: List<MessageAttachment> = attachmentDtos?.takeIf { it.size > 1 } ?: emptyList()
        val response = messagesApi.editMessage(
            EditMessageDtos.Request(
                messageId = messageId,
                roomId = roomId,
                newMessage = EditMessageDtos.NewMessage(
                    content = messageText,
                    attachment = attachment,
                    attachments = otherAttachments,
                )
            )
        )
        if (response.data == null) error("Edit message request failed. Error: ${response.err}")
    }

    override suspend fun sendMultipleMessages(
        userIds: List<Long>?,
        roomIds: List<Long>?,
        content: String?,
        attachment: ChatAttachmentPartialUploadModel?,
        attachments: List<ChatAttachmentPartialUploadModel>?
    ) {
        val response = messagesApi.sendMultipleMessages(
            SendMultipleParams(
                userIds = userIds,
                roomIds = roomIds,
                content = content,
                attachment = attachment,
                attachments = attachments,
            )
        )
        if (response.err != null) {
            error(response.err)
        }
    }
}
