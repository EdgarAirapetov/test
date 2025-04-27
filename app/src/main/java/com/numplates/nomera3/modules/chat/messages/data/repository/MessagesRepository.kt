package com.numplates.nomera3.modules.chat.messages.data.repository

import com.numplates.nomera3.data.network.core.ResponseWrapper
import com.numplates.nomera3.modules.chat.messages.data.entity.ForwardMessageEntityResponse
import com.numplates.nomera3.modules.chat.messages.domain.model.MessageAttachmentModel
import com.numplates.nomera3.modules.fileuploads.domain.model.ChatAttachmentPartialUploadModel
import com.numplates.nomera3.modules.userprofile.data.entity.GreetingResponse

interface MessagesRepository {

    suspend fun forwardMessage(
        messageId: String,
        roomId: Long,
        userIds: List<Long>?,
        roomIds: List<Long>?,
        comment: String,
        success: (ResponseWrapper<ForwardMessageEntityResponse>) -> Unit,
        fail: (Exception) -> Unit
    )

    suspend fun sendGreeting(
        userId: Long,
        stickerId: Int?,
        success: (GreetingResponse) -> Unit,
        fail: (Exception) -> Unit
    )

    suspend fun editMessage(
        messageId: String,
        roomId: Long,
        messageText: String?,
        attachments: List<MessageAttachmentModel>?
    )

    suspend fun sendMultipleMessages(
        userIds: List<Long>?,
        roomIds: List<Long>?,
        content: String?,
        attachment: ChatAttachmentPartialUploadModel?,
        attachments: List<ChatAttachmentPartialUploadModel>?,
    )
}
