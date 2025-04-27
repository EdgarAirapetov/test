package com.numplates.nomera3.modules.contentsharing.ui.loader

import com.numplates.nomera3.modules.chat.messages.domain.usecase.SendMultipleMessagesUseCase
import com.numplates.nomera3.modules.contentsharing.ui.infrastructure.MediaType
import com.numplates.nomera3.modules.contentsharing.ui.infrastructure.SharingDataCache
import com.numplates.nomera3.modules.fileuploads.domain.model.ChatAttachmentPartialUploadModel
import kotlinx.coroutines.yield
import javax.inject.Inject
import kotlin.math.ceil
import kotlin.math.min

private const val FIRST_MESSAGE_INDEX = 0
private const val SINGLE_MESSAGE_VALUE = 1
private const val MAX_IMAGES_IN_MESSAGE = 5

class MessageContentManager @Inject constructor(
    private val sendMultipleMessagesUseCase: SendMultipleMessagesUseCase,
) {

    private val sharingDataCache: SharingDataCache = SharingDataCache

    suspend fun sendMessages(
        content: String?,
        link: String
    ) {
        val userIds = sharingDataCache.getUsersIds()
        val roomIds = sharingDataCache.getRoomsIds()
        sendMultipleMessagesUseCase.invoke(userIds, roomIds, link)
        if (content != null) {
            sendMultipleMessagesUseCase.invoke(userIds, roomIds, content)
        }
    }

    suspend fun sendMessages(
        content: String?,
        attachments: List<ChatAttachmentPartialUploadModel>
    ) {
        uploadAttachments(
            userIds = sharingDataCache.getUsersIds(),
            roomIds = sharingDataCache.getRoomsIds(),
            content = content,
            messages = splitMessages(attachments)
        )
    }

    private fun splitMessages(attachments: List<ChatAttachmentPartialUploadModel>): List<Message> {
        val messages = mutableListOf<Message>()
        val images = attachments.filter { it.type == MediaType.IMAGE.value }
        for (i in 0 until ceil(images.size.toFloat() / MAX_IMAGES_IN_MESSAGE).toInt()) {
            val fromIndex = i * MAX_IMAGES_IN_MESSAGE
            val stepSize = min(MAX_IMAGES_IN_MESSAGE, images.size - fromIndex)
            messages.add(Message(images.subList(fromIndex, fromIndex + stepSize)))
        }
        val videos = attachments.filter { it.type == MediaType.VIDEO.value }
        for (i in videos.indices) {
            messages.add(Message(listOf(videos[i])))
        }
        return messages
    }

    private suspend fun uploadAttachments(
        userIds: List<Long>,
        roomIds: List<Long>,
        content: String?,
        messages: List<Message>
    ) {
        messages.forEachIndexed { index, message ->
            yield()
            sendMultipleMessagesUseCase.invoke(
                userIds = userIds,
                roomIds = roomIds,
                content = if (index == FIRST_MESSAGE_INDEX) content else null,
                attachment = message.attachments.takeIf { it.size == SINGLE_MESSAGE_VALUE }?.first(),
                attachments = message.attachments.takeIf { it.size > SINGLE_MESSAGE_VALUE }
            )
        }
    }

    private class Message(
        val attachments: List<ChatAttachmentPartialUploadModel>
    )
}
