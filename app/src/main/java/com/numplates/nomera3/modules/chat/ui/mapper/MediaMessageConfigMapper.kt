package com.numplates.nomera3.modules.chat.ui.mapper

import android.content.Context
import com.meera.core.extensions.empty
import com.meera.core.utils.getDurationSeconds
import com.meera.uikit.widgets.chat.media.UiKitMediaConfig
import com.meera.uikit.widgets.disclaimer.DisclaimerConfig
import com.meera.uikit.widgets.disclaimer.ResourceConfig
import com.numplates.nomera3.R
import com.numplates.nomera3.data.newmessenger.TYPING_TYPE_GIF
import com.numplates.nomera3.data.newmessenger.TYPING_TYPE_IMAGE
import com.numplates.nomera3.modules.chat.ui.model.MessageConfigWrapperUiModel
import com.numplates.nomera3.modules.chat.ui.model.MessageUiModel
import javax.inject.Inject

private const val DEFAULT_RATIO = 1.0
private const val DEFAULT_SIZE = 1080.0

class MediaMessageConfigMapper @Inject constructor(
    private val context: Context,
    replyMessageMapper: ReplyMessageMapper,
) : BaseConfigMapper(context, replyMessageMapper) {

    override fun getConfig(message: MessageUiModel, isGroupChat: Boolean): MessageConfigWrapperUiModel {
        return MessageConfigWrapperUiModel.Media(
            UiKitMediaConfig(
                message = message.content.rawText,
                isMe = message.isMy,
                statusConfig = getMessageStatusConfig(message),
                disclaimerConfig = DisclaimerConfig(
                    disclaimer = message.attachments?.attachments?.firstOrNull()?.isShowImageBlur ?: false,
                    isMyMessage = message.isMy,
                    resources = getMedias(message),
                    messageText = context.getString(R.string.chat_request_media_blur_description),
                    buttonText = context.getString(R.string.general_show),
                ),
                replyConfig = getMessageReplyConfig(message),
                forwardConfig = getMessageForwardConfig(message),
                headerConfig = getMessageHeaderNameConfig(message, isGroupChat)
            ),
        )
    }

    private fun getMedias(message: MessageUiModel): List<ResourceConfig> {
        val medias = mutableListOf<ResourceConfig>()
        message.attachments?.attachments?.forEach { attachment ->
            val width = DEFAULT_SIZE.toInt()
            val height = (DEFAULT_SIZE / (attachment.ratio ?: DEFAULT_RATIO)).toInt()
            val url = attachment.url ?: String.empty()
            val config = when (attachment.type) {
                TYPING_TYPE_GIF -> ResourceConfig.GifConfig(
                    url = url,
                    width = width,
                    height = height,
                    zoomable = true
                )

                TYPING_TYPE_IMAGE -> ResourceConfig.ImageConfig(
                    url = url,
                    width = width,
                    height = height,
                    zoomable = true
                )

                else -> ResourceConfig.VideoConfig(
                    url = url,
                    width = width,
                    height = height,
                    zoomable = true,
                    chipLeft = getDurationSeconds(attachment.duration.toInt())
                )
            }
            medias.add(config)
        }
        return medias
    }
}
