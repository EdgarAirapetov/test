package com.numplates.nomera3.modules.chat.ui.model

import com.meera.core.utils.EmojiUtils
import com.meera.uikit.widgets.roomcell.SendStatus

data class MessageUiModel(
    val id: String,
    val author: MessageUserUiModel?,
    val roomId: Long = 0L,
    val isMy: Boolean,
    val chatItemType: String,
    val creator: MessageUserUiModel?,
    val content: MessageContentUiModel,
    val attachments: MessageAttachmentsUiModel?,
    val metadata: MessageMetadataUiModel?,
    val parentMessage: MessageParentUiModel?,
    val createdAt: Long,
    val editedAt: Long,
    val isDeleted: Boolean,
    val sendStatus: SendStatus,
    val messageType: MessageType,
    val isResendProgress: Boolean = false,
    val isShowLoadingProgress: Boolean = false,
    val isResendAvailable: Boolean = true,
    val isEditingProgress: Boolean = false,
    val refreshMessageItem: Int = 0,
    val isShowUnreadDivider: Boolean = false,
    val isShowGiphyWatermark: Boolean? = false,
    val birthdayRangesList: List<IntRange>? = null
)

fun MessageUiModel.isEmojis(): Boolean {
    val pair = EmojiUtils.stringEmojiData(content.rawText.toString().trim())
    val isHaveText = pair.first
    val countEmoji = pair.second
    return messageType == MessageType.TEXT && !isHaveText && countEmoji > 0
}
