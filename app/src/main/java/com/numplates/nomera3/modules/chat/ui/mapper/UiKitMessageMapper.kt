package com.numplates.nomera3.modules.chat.ui.mapper

import com.meera.uikit.widgets.chat.container.ContainerParams
import com.meera.uikit.widgets.chat.container.UiKitMessagesContainerConfig
import com.meera.uikit.widgets.roomcell.SendStatus
import com.meera.uikit.widgets.userpic.UserpicSizeEnum
import com.meera.uikit.widgets.userpic.UserpicStoriesStateEnum
import com.meera.uikit.widgets.userpic.UserpicUiModel
import com.numplates.nomera3.modules.chat.ui.model.ChatMessageDataUiModel
import com.numplates.nomera3.modules.chat.ui.model.MessageConfigWrapperUiModel
import com.numplates.nomera3.modules.chat.ui.model.MessageType
import com.numplates.nomera3.modules.chat.ui.model.MessageUiModel
import com.numplates.nomera3.modules.chat.ui.model.isEmojis
import timber.log.Timber
import javax.inject.Inject

class UiKitMessageMapper @Inject constructor(
    private val callMessageConfigMapper: CallMessageConfigMapper,
    private val textMessageConfigMapper: TextMessageConfigMapper,
    private val stickerMessageConfigMapper: StickerMessageConfigMapper,
    private val communityMessageConfigMapper: CommunityMessageConfigMapper,
    private val profileMessageConfigMapper: ProfileMessageConfigMapper,
    private val deletedMessageConfigMapper: DeletedMessageConfigMapper,
    private val repostMessageConfigMapper: RepostMessageConfigMapper,
    private val giftMessageConfigMapper: GiftMessageConfigMapper,
    private val audioMessageConfigMapper: AudioMessageConfigMapper,
    private val momentMessageConfigMapper: MomentMessageConfigMapper,
    private val mediaMessageConfigMapper: MediaMessageConfigMapper
) {

    fun mapToUiKitChatMessagesData(
        messages: List<MessageUiModel>?,
        isGroupChat: Boolean
    ): List<ChatMessageDataUiModel> {
        return messages.orEmpty().map { mapToUiKitChatMessageData(it, isGroupChat) }
    }

    fun mapToUiKitChatMessageData(message: MessageUiModel, isGroupChat: Boolean): ChatMessageDataUiModel {
        Timber.d("mapToUiKitChatMessageData; message: $message, isGroupChat: $isGroupChat")
        return ChatMessageDataUiModel(
            messageData = message,
            messageConfig = getUiMessageConfig(message, isGroupChat),
            containerConfig = UiKitMessagesContainerConfig(
                isMyMessage = isMyMessage(message),
                hasError = hasMessageError(message),
                userpicUiModel = getUserPickConfig(message, isGroupChat),
                containerParams = getContainerParams(message)
            ),
        )
    }

    private fun getUiMessageConfig(message: MessageUiModel, isGroupChat: Boolean): MessageConfigWrapperUiModel {
        Timber.d("getUiMessageConfig() called with messageType: ${message.messageType}, isGroupChat: $isGroupChat")
        val config = when (message.messageType) {
            MessageType.TEXT -> textMessageConfigMapper.getConfig(message, isGroupChat)
            MessageType.IMAGE,
            MessageType.LIST,
            MessageType.VIDEO,
            MessageType.GIF -> mediaMessageConfigMapper.getConfig(message, isGroupChat)
            MessageType.GREETING,
            MessageType.STICKER -> stickerMessageConfigMapper.getConfig(message, isGroupChat)
            MessageType.REPOST -> repostMessageConfigMapper.getConfig(message, isGroupChat)
            MessageType.CALL -> callMessageConfigMapper.getConfig(message, isGroupChat)
            MessageType.AUDIO -> audioMessageConfigMapper.getConfig(message, isGroupChat)
            MessageType.SHARE_PROFILE -> profileMessageConfigMapper.getConfig(message, isGroupChat)
            MessageType.SHARE_COMMUNITY -> communityMessageConfigMapper.getConfig(message, isGroupChat)
            MessageType.GIFT -> giftMessageConfigMapper.getConfig(message, isGroupChat)
            MessageType.DELETED -> deletedMessageConfigMapper.getConfig(message, isGroupChat)
            MessageType.MOMENT -> momentMessageConfigMapper.getConfig(message, isGroupChat)
            else -> MessageConfigWrapperUiModel.NotImplemented
        }
        Timber.d("getUiMessageConfig() returning config: $config")
        return config
    }

    private fun hasMessageError(message: MessageUiModel): Boolean {
        val hasError = message.sendStatus == SendStatus.ERROR
        Timber.d("hasMessageError() called for message: ${message.id}, returning: $hasError")
        return hasError
    }

    private fun isMyMessage(message: MessageUiModel): Boolean {
        Timber.d("isMyMessage() called for message: ${message.id}, returning: ${message.isMy}")
        return message.isMy
    }

    private fun getUserPickConfig(message: MessageUiModel, isGroupChat: Boolean): UserpicUiModel? {
        Timber.d("getUserPickConfig() called with isGroupChat: $isGroupChat, message: ${message.id}")
        val config = if (isGroupChat && !message.isMy) {
            UserpicUiModel(
                size = UserpicSizeEnum.Size40,
                storiesState = UserpicStoriesStateEnum.NO_STORIES,
                userAvatarShow = true,
                userAvatarUrl = message.creator?.avatar
            )
        } else {
            null
        }
        Timber.d("getUserPickConfig() returning config: $config")
        return config
    }

    private fun getContainerParams(message: MessageUiModel): ContainerParams {
        val isEmojis = message.isEmojis()
        val isSticker = message.messageType == MessageType.STICKER
        val isRepost = message.messageType == MessageType.REPOST
        val params = when {
            isEmojis || isSticker || isRepost -> ContainerParams.MATCH
            else -> ContainerParams.WRAP
        }
        Timber.d("getContainerParams() called for messageType: ${message.messageType}, returning: $params")
        return params
    }
}
