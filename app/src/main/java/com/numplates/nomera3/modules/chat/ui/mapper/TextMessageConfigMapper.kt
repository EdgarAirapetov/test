package com.numplates.nomera3.modules.chat.ui.mapper

import android.content.Context
import com.meera.uikit.widgets.chat.emoji.UiKitEmojiConfig
import com.meera.uikit.widgets.chat.regular.UiKitRegularConfig
import com.meera.uikit.widgets.chat.status.StatusStyle
import com.numplates.nomera3.modules.chat.ui.model.MessageConfigWrapperUiModel
import com.numplates.nomera3.modules.chat.ui.model.MessageUiModel
import com.numplates.nomera3.modules.chat.ui.model.isEmojis
import javax.inject.Inject

class TextMessageConfigMapper @Inject constructor(
    context: Context,
    replyMessageMapper: ReplyMessageMapper,
) : BaseConfigMapper(context, replyMessageMapper) {

    override fun getConfig(message: MessageUiModel, isGroupChat: Boolean): MessageConfigWrapperUiModel {
        val content = message.content.rawText.toString().trim()
        if (message.isEmojis()) {
            return MessageConfigWrapperUiModel.Emoji(
                UiKitEmojiConfig(
                    messageText = content,
                    statusConfig = getMessageStatusConfig(message, StatusStyle.OUTLINE),
                    isMe = message.isMy,
                    replyConfig = getMessageReplyConfig(message),
                    forwardConfig = getMessageForwardConfig(message),
                )
            )
        } else {
            return MessageConfigWrapperUiModel.Default(
                UiKitRegularConfig(
                    message = message.content.tagSpan?.text.toString(),
                    statusConfig = getMessageStatusConfig(message),
                    replyConfig = getMessageReplyConfig(message),
                    isMe = message.isMy,
                    forwardConfig = getMessageForwardConfig(message),
                    headerConfig = getMessageHeaderNameConfig(message, isGroupChat),
                )
            )
        }
    }
}
