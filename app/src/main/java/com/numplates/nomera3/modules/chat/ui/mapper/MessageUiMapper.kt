package com.numplates.nomera3.modules.chat.ui.mapper

import com.meera.db.models.dialog.UserChat
import com.meera.db.models.message.MessageAttachment
import com.meera.db.models.message.MessageEntity
import com.meera.db.models.message.MessageMetadata
import com.meera.db.models.message.ParentMessage
import com.meera.uikit.widgets.roomcell.SendStatus
import com.numplates.nomera3.data.newmessenger.CHAT_ITEM_TYPE_EVENT
import com.numplates.nomera3.domain.interactornew.GetUserUidUseCase
import com.numplates.nomera3.modules.chat.messages.presentation.ITEM_TYPE_AUDIO_RECEIVE
import com.numplates.nomera3.modules.chat.messages.presentation.ITEM_TYPE_AUDIO_SEND
import com.numplates.nomera3.modules.chat.messages.presentation.ITEM_TYPE_CALLS
import com.numplates.nomera3.modules.chat.messages.presentation.ITEM_TYPE_DATE_DIVIDER
import com.numplates.nomera3.modules.chat.messages.presentation.ITEM_TYPE_GIFT_RECEIVE
import com.numplates.nomera3.modules.chat.messages.presentation.ITEM_TYPE_GIFT_SEND
import com.numplates.nomera3.modules.chat.messages.presentation.ITEM_TYPE_GREETING_RECEIVE
import com.numplates.nomera3.modules.chat.messages.presentation.ITEM_TYPE_GREETING_SEND
import com.numplates.nomera3.modules.chat.messages.presentation.ITEM_TYPE_IMAGE_RECEIVE
import com.numplates.nomera3.modules.chat.messages.presentation.ITEM_TYPE_IMAGE_SEND
import com.numplates.nomera3.modules.chat.messages.presentation.ITEM_TYPE_MESSAGE_RECEIVE_DELETED
import com.numplates.nomera3.modules.chat.messages.presentation.ITEM_TYPE_MESSAGE_SEND_DELETED
import com.numplates.nomera3.modules.chat.messages.presentation.ITEM_TYPE_ONLY_TEXT_RECEIVE
import com.numplates.nomera3.modules.chat.messages.presentation.ITEM_TYPE_ONLY_TEXT_SEND
import com.numplates.nomera3.modules.chat.messages.presentation.ITEM_TYPE_REPOST_RECEIVE
import com.numplates.nomera3.modules.chat.messages.presentation.ITEM_TYPE_REPOST_SEND
import com.numplates.nomera3.modules.chat.messages.presentation.ITEM_TYPE_SHARE_COMMUNITY_RECEIVE
import com.numplates.nomera3.modules.chat.messages.presentation.ITEM_TYPE_SHARE_COMMUNITY_SEND
import com.numplates.nomera3.modules.chat.messages.presentation.ITEM_TYPE_SHARE_PROFILE_RECEIVE
import com.numplates.nomera3.modules.chat.messages.presentation.ITEM_TYPE_SHARE_PROFILE_SEND
import com.numplates.nomera3.modules.chat.messages.presentation.ITEM_TYPE_STICKER_RECEIVE
import com.numplates.nomera3.modules.chat.messages.presentation.ITEM_TYPE_STICKER_SEND
import com.numplates.nomera3.modules.chat.messages.presentation.ITEM_TYPE_VIDEO_RECEIVE
import com.numplates.nomera3.modules.chat.messages.presentation.ITEM_TYPE_VIDEO_SEND
import com.numplates.nomera3.modules.chat.ui.model.AttachmentUiModel
import com.numplates.nomera3.modules.chat.ui.model.MessageAttachmentsUiModel
import com.numplates.nomera3.modules.chat.ui.model.MessageContentUiModel
import com.numplates.nomera3.modules.chat.ui.model.MessageMetadataCallUiModel
import com.numplates.nomera3.modules.chat.ui.model.MessageMetadataUiModel
import com.numplates.nomera3.modules.chat.ui.model.MessageParentUiModel
import com.numplates.nomera3.modules.chat.ui.model.MessageType
import com.numplates.nomera3.modules.chat.ui.model.MessageUiModel
import com.numplates.nomera3.modules.chat.ui.model.MessageUserUiModel
import com.numplates.nomera3.presentation.model.enums.ChatEventEnum
import timber.log.Timber
import javax.inject.Inject

class MessageUiMapper @Inject constructor(
    private val getUserUidUseCase: GetUserUidUseCase
) {

    fun mapToMessagesUi(messages: List<MessageEntity>?, isSomeoneBirthday: Boolean = false): List<MessageUiModel> {
        return messages.orEmpty().map { message -> mapToMessageUi(message, isSomeoneBirthday) }
    }

    fun mapToMessageUi(message: MessageEntity, isSomeoneBirthday: Boolean = false): MessageUiModel {
        return MessageUiModel(
            id = message.msgId,
            author = getUser(message.author),
            roomId = message.roomId,
            isMy = getUserUidUseCase.invoke() == message.creator?.userId,
            chatItemType = message.type,
            creator = getUser(message.creator),
            content = getMessageContent(message),
            attachments = getAttachments(message),
            metadata = getMetadata(message.metadata),
            parentMessage = mapToParentMessage(message.parentMessage),
            createdAt = message.createdAt,
            editedAt = message.editedAt,
            isDeleted = message.deleted,
            sendStatus = getSendStatus(message),
            messageType = getMessageType(message),
            isResendProgress = message.isResendProgress,
            isShowLoadingProgress = message.isShowLoadingProgress,
            isResendAvailable = message.isResendAvailable,
            isEditingProgress = message.isEditingProgress,
            refreshMessageItem = message.refreshMessageItem,
            isShowUnreadDivider = message.isShowUnreadDivider,
            isShowGiphyWatermark = message.isShowGiphyWatermark,
            birthdayRangesList = getBirthdayRangeList(message, isSomeoneBirthday),
        )
    }

    private fun getMessageType(message: MessageEntity): MessageType {
        val messageType = checkDeletedType(message)
            ?: checkTypeByEventCode(message.eventCode)
            ?: checkTypeByItemType(message.itemType)
            ?: checkByMessageType(message.type)
        Timber.d("message type: $messageType for message: $message")
        return messageType
    }

    private fun checkDeletedType(message: MessageEntity): MessageType? {
        return if (message.deleted) MessageType.DELETED else null
    }

    private fun checkTypeByEventCode(eventCode: Int?): MessageType? {
        return when (eventCode?.let(ChatEventEnum::get)) {
            ChatEventEnum.TEXT -> MessageType.TEXT
            ChatEventEnum.IMAGE -> MessageType.IMAGE
            ChatEventEnum.GIF -> MessageType.GIF
            ChatEventEnum.AUDIO -> MessageType.AUDIO
            ChatEventEnum.VIDEO -> MessageType.VIDEO
            ChatEventEnum.REPOST -> MessageType.REPOST
            ChatEventEnum.LIST -> MessageType.LIST
            ChatEventEnum.GIFT -> MessageType.GIFT
            ChatEventEnum.SHARE_PROFILE -> MessageType.SHARE_PROFILE
            ChatEventEnum.SHARE_COMMUNITY -> MessageType.SHARE_COMMUNITY
            ChatEventEnum.MOMENT -> MessageType.MOMENT
            ChatEventEnum.GREETING -> MessageType.GREETING
            ChatEventEnum.STICKER -> MessageType.STICKER
            ChatEventEnum.CALL -> MessageType.CALL
            else -> null
        }
    }

    private fun checkTypeByItemType(itemType: Int): MessageType? {
        return when (itemType) {
            ITEM_TYPE_DATE_DIVIDER -> MessageType.DATE_TIME
            ITEM_TYPE_CALLS -> MessageType.CALL

            ITEM_TYPE_MESSAGE_RECEIVE_DELETED,
            ITEM_TYPE_MESSAGE_SEND_DELETED -> MessageType.DELETED

            ITEM_TYPE_ONLY_TEXT_SEND,
            ITEM_TYPE_ONLY_TEXT_RECEIVE -> MessageType.TEXT

            ITEM_TYPE_REPOST_SEND,
            ITEM_TYPE_REPOST_RECEIVE -> MessageType.REPOST

            ITEM_TYPE_AUDIO_SEND,
            ITEM_TYPE_AUDIO_RECEIVE -> MessageType.AUDIO

            ITEM_TYPE_VIDEO_SEND,
            ITEM_TYPE_VIDEO_RECEIVE -> MessageType.VIDEO

            ITEM_TYPE_GIFT_SEND,
            ITEM_TYPE_GIFT_RECEIVE -> MessageType.GIFT

            ITEM_TYPE_STICKER_SEND,
            ITEM_TYPE_STICKER_RECEIVE -> MessageType.STICKER

            ITEM_TYPE_SHARE_PROFILE_SEND,
            ITEM_TYPE_SHARE_PROFILE_RECEIVE -> MessageType.SHARE_PROFILE

            ITEM_TYPE_SHARE_COMMUNITY_SEND,
            ITEM_TYPE_SHARE_COMMUNITY_RECEIVE -> MessageType.SHARE_COMMUNITY

            ITEM_TYPE_GREETING_SEND,
            ITEM_TYPE_GREETING_RECEIVE -> MessageType.GREETING

            ITEM_TYPE_IMAGE_SEND,
            ITEM_TYPE_IMAGE_RECEIVE -> MessageType.IMAGE

            else -> null
        }
    }

    private fun checkByMessageType(type: String?): MessageType {
        return when (type) {
            CHAT_ITEM_TYPE_EVENT -> MessageType.EVENT
            else -> MessageType.OTHER
        }
    }

    private fun getMessageContent(message: MessageEntity): MessageContentUiModel {
        return MessageContentUiModel(
            isExists = message.content.isEmpty().not(),
            rawText = message.content,
            tagSpan = message.tagSpan
        )
    }

    private fun getUser(user: UserChat?): MessageUserUiModel? {
        if (user == null) return null
        return MessageUserUiModel(
            id = user.userId ?: 0L,
            name = user.name,
            avatar = user.avatarSmall
        )
    }

    private fun getMetadata(meta: MessageMetadata?): MessageMetadataUiModel? {
        if (meta == null) return null
        return MessageMetadataUiModel(
            userId = meta.userId,
            type = meta.type,
            status = meta.status,
            createdAt = meta.createdAt,
            caller = MessageMetadataCallUiModel(
                callerId = meta.caller?.callerId
            ),
            callDuration = meta.callDuration
        )
    }

    private fun getSendStatus(message: MessageEntity): SendStatus {
        val sent = message.sent
        val delivered = message.delivered
        val read = message.readed
        val loading = message.isShowLoadingProgress
        val edited = message.editedAt > 0
        return when {
            loading -> SendStatus.SENDING
            edited -> SendStatus.EDITED
            sent && !(delivered || read) -> SendStatus.SENT
            sent && (delivered && !read) -> SendStatus.DELIVERED
            sent && read -> SendStatus.READ
            !sent -> SendStatus.ERROR
            else -> SendStatus.UNKNOWN
        }
    }

    private fun getAttachments(message: MessageEntity): MessageAttachmentsUiModel {
        val isShowImageBlur = message.isShowImageBlurChatRequest
        return MessageAttachmentsUiModel(
            isMultiple = message.attachments.isNotEmpty() && message.eventCode == ChatEventEnum.LIST.state,
            attachments = when {
                message.attachment.url.isEmpty().not() ->
                    listOf(mapToAttachmentUiModel(message.attachment, isShowImageBlur))

                message.attachments.isEmpty().not() ->
                    message.attachments.map { mapToAttachmentUiModel(it, isShowImageBlur) }

                else -> null
            }
        )
    }

    private fun mapToAttachmentUiModel(
        attachment: MessageAttachment,
        isShowImageBlur: Boolean?
    ): AttachmentUiModel {
        return AttachmentUiModel(
            id = attachment.id,
            favoriteId = attachment.favoriteId,
            url = attachment.url,
            lottieUrl = attachment.lottieUrl,
            webpUrl = attachment.webpUrl,
            type = attachment.type,
            isShowImageBlur = isShowImageBlur,
            metadata = attachment.metadata
        )
    }

    private fun mapToParentMessage(message: ParentMessage?): MessageParentUiModel? {
        if (message == null) return null
        return MessageParentUiModel(
            messageType = checkTypeByEventCode(message.eventCode) ?: MessageType.OTHER,
            creatorName = message.creatorName,
            messageContent = message.messageContent,
            imagePreview = message.imagePreview,
            videoPreview = message.videoPreview,
            imageCount = message.imageCount,
            createdAt = message.createdAt,
            parentId = message.parentId,
            sharedProfileUrl = message.sharedProfileUrl,
            isDeletedSharedProfile = message.isDeletedSharedProfile,
            sharedCommunityUrl = message.sharedCommunityUrl,
            isPrivateCommunity = message.isPrivateCommunity,
            isDeletedSharedCommunity = message.isDeletedSharedCommunity,
            isEvent = message.isEvent,
            isMoment = message.isMoment,
            metadata = getMetadata(message.metadata),
        )
    }

    private fun getBirthdayRangeList(message: MessageEntity, isSomeoneBirthday: Boolean): List<IntRange>? {
        return if (isSomeoneBirthday) {
            message.birthdayRangesList
        } else {
            null
        }
    }
}
