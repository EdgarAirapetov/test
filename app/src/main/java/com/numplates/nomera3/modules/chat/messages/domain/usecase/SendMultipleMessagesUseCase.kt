package com.numplates.nomera3.modules.chat.messages.domain.usecase

import com.numplates.nomera3.modules.chat.messages.data.repository.MessagesRepository
import com.numplates.nomera3.modules.fileuploads.domain.model.ChatAttachmentPartialUploadModel
import javax.inject.Inject

class SendMultipleMessagesUseCase @Inject constructor(
    private val repository: MessagesRepository
) {

    suspend fun invoke(
        userIds: List<Long>?,
        roomIds: List<Long>?,
        content: String? = null,
        attachment: ChatAttachmentPartialUploadModel? = null,
        attachments: List<ChatAttachmentPartialUploadModel>? = null,
    ) {
        check(userIds != null || roomIds != null) {
            "One of the params required: userIds or roomIds."
        }
        check(content != null || (attachment != null || attachments != null)) {
            "content param is required if attachments are empty."
        }
        repository.sendMultipleMessages(
            userIds = userIds,
            roomIds = roomIds,
            content = content,
            attachment = attachment,
            attachments = attachments,
        )
    }
}
