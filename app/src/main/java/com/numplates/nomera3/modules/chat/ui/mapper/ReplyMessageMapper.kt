package com.numplates.nomera3.modules.chat.ui.mapper

import android.content.Context
import androidx.annotation.DrawableRes
import com.meera.uikit.widgets.chat.MessengerMedia
import com.meera.uikit.widgets.chat.call.CallType
import com.meera.uikit.widgets.chat.reply.ReplyMode
import com.meera.uikit.widgets.chat.reply.UiKitReplyConfig
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.chat.helpers.isGifUrl
import com.numplates.nomera3.modules.chat.ui.model.MessageParentUiModel
import com.numplates.nomera3.modules.chat.ui.model.MessageType
import com.numplates.nomera3.modules.chat.ui.model.MessageUiModel
import com.numplates.nomera3.modules.chat.ui.model.isEmojis
import timber.log.Timber
import java.util.Locale
import javax.inject.Inject


class ReplyMessageMapper @Inject constructor(
    private val context: Context,
    private val callDataMapper: CallDataMapper,
) {

    fun mapMessageToReplyConfig(message: MessageUiModel): UiKitReplyConfig? {
        Timber.d("Mapping message to reply config: ${message.id}")
        if (message.parentMessage == null) {
            Timber.d("No parent message found")
            return null
        }
        val parent = message.parentMessage
        val avatar = getAvatar(parent)
        Timber.d("Parent message: $parent, avatar: $avatar")
        return UiKitReplyConfig(
            media = avatar,
            mode = getReplyMode(avatar, message),
            replyHeader = parent.creatorName,
            replyDescription = getReplyTypeDescription(parent),
            replyDrawableRes = getReplyTypeDrawableRes(parent)
        ).also {
            Timber.d("Reply config created: $it")
        }
    }

    private fun getAvatar(parent: MessageParentUiModel): MessengerMedia? {
        Timber.d("Getting avatar for parent message type: ${parent.messageType}")
        return when (parent.messageType) {
            MessageType.LIST,
            MessageType.IMAGE,
            MessageType.STICKER,
            MessageType.GIF -> MessengerMedia.Url(parent.imagePreview)

            MessageType.VIDEO -> MessengerMedia.Url(parent.videoPreview)
            MessageType.MOMENT,
            MessageType.REPOST -> MessengerMedia.Drawable(R.drawable.ic_outlined_post_m)

            MessageType.SHARE_PROFILE,
            MessageType.SHARE_COMMUNITY -> {
                val avatarRes = when {
                    parent.isDeletedSharedProfile -> R.drawable.chat_profile_deleted
                    parent.isDeletedSharedCommunity -> R.drawable.chat_profile_deleted
                    parent.imagePreview.isEmpty() -> R.drawable.chat_profile_deleted
                    else -> -1
                }
                MessengerMedia.Drawable(avatarRes)
            }

            else -> null
        }.also {
            Timber.d("Avatar: $it")
        }
    }

    private fun getReplyMode(avatar: MessengerMedia?, message: MessageUiModel): ReplyMode {
        Timber.d("Getting reply mode for avatar: $avatar, message type: ${message.messageType}")
        val isEmojis = message.isEmojis()
        val isSticker = message.messageType == MessageType.STICKER
        return when {
            (isSticker || isEmojis) && avatar != null -> ReplyMode.REPLY_OUTLINE_WITH_IMAGE
            (isSticker || isEmojis) && avatar == null -> ReplyMode.REPLY_OUTLINE
            avatar != null -> ReplyMode.REPLY_DEFAULT_WITH_IMAGE
            else -> ReplyMode.REPLY_DEFAULT
        }.also {
            Timber.d("Reply mode: $it")
        }
    }

    @DrawableRes
    private fun getReplyTypeDrawableRes(parent: MessageParentUiModel): Int? {
        Timber.d("Getting reply type drawable resource for parent message type: ${parent.messageType}")
        return when (parent.messageType) {
            MessageType.AUDIO -> R.drawable.ic_outlined_mic_s
            MessageType.CALL -> {
                val callData = callDataMapper.mapToCallData(parent.metadata)
                return when (callData.iconType) {
                    CallType.OUTGOING -> R.drawable.ic_outlined_call_out_s
                    CallType.INCOMING -> R.drawable.ic_outlined_call_in_s
                    CallType.DECLINED,
                    CallType.MISSED -> R.drawable.ic_outlined_call_missed_s
                }
            }

            else -> null
        }.also {
            Timber.d("Reply type drawable resource: $it")
        }
    }

    private fun getReplyTypeDescription(parent: MessageParentUiModel): String {
        Timber.d("Getting reply type description for parent message type: ${parent.messageType}")
        return when (parent.messageType) {
            MessageType.LIST -> context.getString(R.string.chat_edit_preview_photos, parent.imageCount)
            MessageType.GIF -> context.getString(R.string.general_gif_uppercase)
            MessageType.STICKER -> context.getString(R.string.sticker)
            MessageType.SHARE_PROFILE -> context.getString(R.string.profile_info)
            MessageType.AUDIO -> context.getString(R.string.audio_message)
            MessageType.IMAGE -> parent.messageContent.ifBlank {
                val title = if (parent.imagePreview.isGifUrl()) {
                    context.getString(R.string.general_gif_uppercase)
                } else {
                    context.getString(R.string.profile_photo)
                }
                if (parent.imageCount == 1 || parent.imageCount == 0) {
                    title
                } else {
                    "${parent.imageCount} ${title.lowercase(Locale.getDefault())}"
                }
            }

            MessageType.MOMENT -> parent.messageContent.ifBlank {
                context.getString(R.string.moment_title)
            }

            MessageType.VIDEO -> parent.messageContent.ifBlank {
                context.getString(R.string.video)
            }

            MessageType.SHARE_COMMUNITY -> if (parent.isPrivateCommunity) {
                context.getString(R.string.group_edit_fragment_close_option_name)
            } else {
                context.getString(R.string.group_edit_fragment_open_option_name)
            }

            MessageType.REPOST -> if (parent.isEvent) {
                context.getString(R.string.map_events_configuration_title)
            } else if (parent.isMoment) {
                parent.messageContent.takeIf { it.isNotBlank() }
                    ?: context.getString(R.string.chat_repost_moment_message)
            } else {
                context.getString(R.string.chat_repost_message)
            }

            MessageType.CALL -> {
                callDataMapper.mapToCallData(parent.metadata).title
            }

            else -> parent.messageContent
        }.also {
            Timber.d("Reply type description: $it")
        }
    }
}
