package com.numplates.nomera3.modules.chat.ui.helper

import android.view.View
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.meera.core.extensions.fromJson
import com.meera.core.extensions.getDrawableCompat
import com.meera.core.extensions.gone
import com.meera.core.extensions.setImageDrawable
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.toBoolean
import com.meera.core.extensions.visible
import com.meera.db.models.userprofile.UserSimple
import com.meera.uikit.widgets.chat.MessengerMedia
import com.meera.uikit.widgets.chat.call.CallType
import com.meera.uikit.widgets.chat.reply.ReplyMode
import com.numplates.nomera3.R
import com.numplates.nomera3.data.network.MediaAssetDto
import com.numplates.nomera3.data.network.Post
import com.numplates.nomera3.data.newmessenger.CHAT_ITEM_TYPE_EVENT
import com.numplates.nomera3.data.newmessenger.TYPING_TYPE_MOMENT
import com.numplates.nomera3.databinding.MeeraLayoutChatReplyMenuBinding
import com.numplates.nomera3.modules.chat.helpers.isGifUrl
import com.numplates.nomera3.modules.chat.ui.mapper.CallDataMapper
import com.numplates.nomera3.modules.chat.ui.model.MessageType
import com.numplates.nomera3.modules.chat.ui.model.MessageUiModel
import com.numplates.nomera3.modules.communities.data.entity.CommunityShareEntity
import com.numplates.nomera3.modules.moments.show.data.MomentItemDto
import timber.log.Timber
import java.util.Locale

class MeeraReplyHelper(
    private val binding: MeeraLayoutChatReplyMenuBinding,
    private val callDataMapper: CallDataMapper,
) {

    private val context get() = binding.root.context
    private val gson: Gson = Gson()

    val closeBtn: ImageView = binding.ivCloseReply
    var menuListener: (Boolean) -> Unit = { }

    init {
        binding.ivCloseReply.setThrottledClickListener { hideReplyContainer() }
    }

    fun showReply(message: MessageUiModel, listener: () -> Unit) {
        Timber.d("showReply() called with message: $message")
        binding.root.setThrottledClickListener(clickListener = listener)
        bindReplyContainer(message)
        showReplyContainer()
    }

    fun hideReply() {
        Timber.d("hideReply() called")
        if (isMenuVisible()) hideReplyContainer()
    }

    fun isMenuVisible(): Boolean {
        val isVisible = binding.root.visibility == View.VISIBLE
        Timber.d("isMenuVisible() returning: $isVisible")
        return isVisible
    }

    private fun bindReplyContainer(message: MessageUiModel) {
        Timber.d("Mapping message to reply config: ${message.id}")
        val avatar = getAvatar(message)
        Timber.d("Parent message: $message, avatar: $avatar")
        bindMedia(
            mode = getReplyMode(avatar, message),
            avatar = avatar
        )
        binding.tvReplyTitle.text = message.creator?.name.orEmpty()
        binding.tvReplyMessage.text = getReplyTypeDescription(message)
        binding.tvReplyMessage.setCompoundDrawablesRelativeWithIntrinsicBounds(
            getReplyTypeDrawableRes(message)?.let(context::getDrawableCompat),
            null,
            null,
            null
        )
    }

    private fun showReplyContainer() {
        Timber.d("showReplyContainer() called")
        binding.root.visible()
        menuListener(true)
    }

    private fun hideReplyContainer() {
        Timber.d("hideReplyContainer() called")
        binding.root.gone()
        menuListener(false)
    }

    private fun bindMedia(
        mode: ReplyMode,
        avatar: MessengerMedia?
    ) {
        val hasMedia = avatar is MessengerMedia.Url || avatar is MessengerMedia.Drawable
        val withImage = mode == ReplyMode.REPLY_DEFAULT_WITH_IMAGE || mode == ReplyMode.REPLY_OUTLINE_WITH_IMAGE
        binding.vgImageContainer.isVisible = hasMedia && withImage
        when (avatar) {
            is MessengerMedia.Drawable -> {
                binding.ivReplyImage.scaleType = ImageView.ScaleType.CENTER_INSIDE
                binding.ivReplyImage.background = context.getDrawableCompat(R.drawable.meera_bg_oval_back_secondary)
                binding.ivReplyImage.setImageDrawable(avatar.drawableRes)
            }

            is MessengerMedia.Url -> {
                binding.ivReplyImage.scaleType = ImageView.ScaleType.CENTER_CROP
                binding.ivReplyImage.background = null
                Glide.with(binding.ivReplyImage)
                    .load(avatar.url)
                    .into(binding.ivReplyImage)
            }

            else -> {
                binding.ivReplyImage.scaleType = ImageView.ScaleType.CENTER_CROP
                binding.ivReplyImage.background = null
                binding.ivReplyImage.setImageDrawable(null)
            }
        }
    }

    private fun getAvatar(message: MessageUiModel): MessengerMedia? {
        Timber.d("Getting avatar for message type: ${message.messageType}")
        val attachment = message.attachments?.attachments?.firstOrNull()
        Timber.d("Getting attachment metadata: ${attachment?.metadata}")
        return when (message.messageType) {
            MessageType.LIST,
            MessageType.IMAGE,
            MessageType.STICKER,
            MessageType.GIF,
            MessageType.VIDEO -> {
                MessengerMedia.Url(attachment?.url)
            }

            MessageType.MOMENT -> {
                val moment = gson.fromJson<MomentItemDto?>(attachment?.moment.orEmpty())
                getUrlOrPlaceholder(moment?.asset?.preview, R.drawable.ic_outlined_post_m)
            }

            MessageType.REPOST -> {
                val post = gson.fromJson<Post?>(attachment?.repost.orEmpty())
                getUrlOrPlaceholder(getPostImage(post), R.drawable.ic_outlined_post_m)
            }

            MessageType.SHARE_PROFILE -> {
                val user = gson.fromJson<UserSimple?>(attachment?.metadata.orEmpty())
                getUrlOrPlaceholder(user?.avatarSmall, R.drawable.ic_outlined_photo_m)
            }

            MessageType.SHARE_COMMUNITY -> {
                val community = gson.fromJson<CommunityShareEntity?>(attachment?.metadata.orEmpty())
                getUrlOrPlaceholder(community?.avatar, R.drawable.ic_outlined_photo_m)
            }

            else -> null
        }.also {
            Timber.d("Avatar: $it")
        }
    }

    private fun getUrlOrPlaceholder(url: String?, placeholderRes: Int): MessengerMedia {
        Timber.d("Getting getUrlOrPlaceholder with url: $url, placeholderRes: $placeholderRes")
        return when {
            !url.isNullOrBlank() -> MessengerMedia.Url(url)
            else -> MessengerMedia.Drawable(placeholderRes)
        }
    }

    private fun getPostImage(post: Post?): String? {
        return if (post?.deleted.toBoolean()) {
            null
        } else {
            when {
                post?.asset?.metadata?.preview != null -> post.asset?.metadata?.preview
                post?.asset?.url != null -> post.asset?.url
                post?.assets?.firstOrNull() != null -> getUrlFromMedia(requireNotNull(post.assets?.first()))
                else -> null
            }
        }
    }

    private fun getUrlFromMedia(media: MediaAssetDto): String? {
        return when {
            !media.metadata?.preview.isNullOrEmpty() -> media.metadata?.preview
            !media.metadata?.smallUrl.isNullOrEmpty() -> media.metadata?.smallUrl
            !media.image.isNullOrEmpty() -> media.image
            !media.videoPreview.isNullOrEmpty() -> media.videoPreview
            else -> media.video
        }
    }

    private fun getReplyMode(avatar: MessengerMedia?, message: MessageUiModel): ReplyMode {
        Timber.d("Getting reply mode for avatar: $avatar, message type: ${message.messageType}")
        return when {
            avatar != null -> ReplyMode.REPLY_DEFAULT_WITH_IMAGE
            else -> ReplyMode.REPLY_DEFAULT
        }.also {
            Timber.d("Reply mode: $it")
        }
    }

    @DrawableRes
    private fun getReplyTypeDrawableRes(message: MessageUiModel): Int? {
        Timber.d("Getting reply type drawable resource for parent message type: ${message.messageType}")
        return when (message.messageType) {
            MessageType.AUDIO -> R.drawable.ic_outlined_mic_s
            MessageType.CALL -> {
                val callData = callDataMapper.mapToCallData(message.metadata)
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

    private fun getReplyTypeDescription(message: MessageUiModel): String {
        Timber.d("Getting reply type description for message message type: ${message.messageType}")
        val messageText = message.content.tagSpan?.text.orEmpty()
        val attachments = message.attachments?.attachments
        return when (message.messageType) {
            MessageType.LIST -> context.getString(
                R.string.chat_edit_preview_photos,
                message.attachments?.attachments?.size ?: 0
            )

            MessageType.GIF -> context.getString(R.string.general_gif_uppercase)
            MessageType.STICKER -> context.getString(R.string.sticker)
            MessageType.SHARE_PROFILE -> context.getString(R.string.profile_info)
            MessageType.AUDIO -> context.getString(R.string.audio_message)
            MessageType.IMAGE -> messageText.ifBlank {
                val imageCount = attachments?.size ?: 0
                val title = if (attachments?.firstOrNull()?.url?.isGifUrl() == true) {
                    context.getString(R.string.general_gif_uppercase)
                } else {
                    context.getString(R.string.profile_photo)
                }
                if (imageCount == 1 || imageCount == 0) {
                    title
                } else {
                    "$imageCount ${title.lowercase(Locale.getDefault())}"
                }
            }

            MessageType.MOMENT -> messageText.ifBlank {
                context.getString(R.string.moment_title)
            }

            MessageType.VIDEO -> messageText.ifBlank {
                context.getString(R.string.video)
            }

            MessageType.SHARE_COMMUNITY -> {
                val community = gson.fromJson<CommunityShareEntity?>(attachments?.firstOrNull()?.metadata.orEmpty())
                if (community?.private == 1) {
                    context.getString(R.string.group_edit_fragment_close_option_name)
                } else {
                    context.getString(R.string.group_edit_fragment_open_option_name)
                }
            }

            MessageType.REPOST -> {
                when (attachments?.firstOrNull()?.type) {
                    CHAT_ITEM_TYPE_EVENT -> context.getString(R.string.map_events_configuration_title)
                    TYPING_TYPE_MOMENT -> messageText.takeIf { it.isNotBlank() }
                        ?: context.getString(R.string.chat_repost_moment_message)

                    else -> context.getString(R.string.chat_repost_message)
                }
            }

            MessageType.CALL -> {
                callDataMapper.mapToCallData(message.metadata).title
            }

            else -> message.content.tagSpan?.text.orEmpty()
        }.also {
            Timber.d("Reply type description: $it")
        }
    }
}
