package com.numplates.nomera3.modules.chat.ui.mapper

import android.content.Context
import com.meera.core.extensions.empty
import com.meera.uikit.widgets.chat.MessengerMedia
import com.meera.uikit.widgets.chat.gift.UiKitGiftConfig
import com.numplates.nomera3.modules.chat.ui.model.MessageConfigWrapperUiModel
import com.numplates.nomera3.modules.chat.ui.model.MessageUiModel
import javax.inject.Inject

class GiftMessageConfigMapper @Inject constructor(
    context: Context,
    replyMessageMapper: ReplyMessageMapper
) : BaseConfigMapper(context, replyMessageMapper) {

    override fun getConfig(message: MessageUiModel, isGroupChat: Boolean): MessageConfigWrapperUiModel {
        val attachment = message.attachments?.attachments?.first()
        val giftUrl = attachment?.url ?: String.empty()
        return MessageConfigWrapperUiModel.Gift(
            UiKitGiftConfig(
                statusConfig = getMessageStatusConfig(message),
                isMe = message.isMy,
                giftAvatar = MessengerMedia.Url(giftUrl),
                giftMessage = message.content.rawText.takeIf { !it.isNullOrBlank() },
                replyConfig = getMessageReplyConfig(message),
                forwardConfig = getMessageForwardConfig(message)
            )
        )
    }
}
