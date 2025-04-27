package com.numplates.nomera3.modules.chat.ui.mapper

import android.content.Context
import com.meera.core.extensions.empty
import com.meera.uikit.widgets.chat.status.StatusStyle
import com.meera.uikit.widgets.chat.sticker.StickerContent
import com.meera.uikit.widgets.chat.sticker.UiKitStickersConfig
import com.numplates.nomera3.modules.chat.ui.model.MessageConfigWrapperUiModel
import com.numplates.nomera3.modules.chat.ui.model.MessageUiModel
import javax.inject.Inject

class StickerMessageConfigMapper @Inject constructor(
    context: Context,
    replyMessageMapper: ReplyMessageMapper
) : BaseConfigMapper(context, replyMessageMapper) {

    override fun getConfig(message: MessageUiModel, isGroupChat: Boolean): MessageConfigWrapperUiModel {
        val url = message.attachments?.attachments?.firstOrNull()?.lottieUrl ?: String.empty()
        return MessageConfigWrapperUiModel.Sticker(
            UiKitStickersConfig(
                stickerContent = StickerContent.LottieUrl(url = url),
                statusConfig = getMessageStatusConfig(
                    message = message,
                    messageStatusStyle = StatusStyle.OUTLINE
                ),
                isMe = message.isMy,
                replyConfig = getMessageReplyConfig(message),
                forwardConfig = getMessageForwardConfig(message),
            )
        )
    }
}

