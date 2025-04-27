package com.numplates.nomera3.modules.chat.helpers

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.gson.Gson
import com.meera.core.extensions.animateHeight
import com.meera.core.extensions.click
import com.meera.core.extensions.dp
import com.meera.core.extensions.empty
import com.meera.core.extensions.fromJson
import com.meera.core.extensions.gone
import com.meera.core.extensions.loadGlide
import com.meera.core.extensions.loadGlideCenterCrop
import com.meera.core.extensions.loadGlideFitCenter
import com.meera.core.extensions.loadGlideProgressive
import com.meera.core.extensions.setMargins
import com.meera.core.extensions.setThrottledClickListener
import com.meera.core.extensions.visible
import com.meera.core.utils.IS_APP_REDESIGNED
import com.meera.db.models.message.MessageEntity
import com.meera.db.models.message.ParentMessage
import com.meera.db.models.userprofile.UserSimple
import com.numplates.nomera3.R
import com.numplates.nomera3.data.newmessenger.CHAT_ITEM_TYPE_EVENT
import com.numplates.nomera3.data.newmessenger.TYPING_TYPE_MOMENT
import com.numplates.nomera3.modules.chat.messages.presentation.ITEM_TYPE_AUDIO_RECEIVE
import com.numplates.nomera3.modules.chat.messages.presentation.ITEM_TYPE_AUDIO_SEND
import com.numplates.nomera3.modules.chat.messages.presentation.ITEM_TYPE_GREETING_RECEIVE
import com.numplates.nomera3.modules.chat.messages.presentation.ITEM_TYPE_GREETING_SEND
import com.numplates.nomera3.modules.chat.messages.presentation.ITEM_TYPE_IMAGE_RECEIVE
import com.numplates.nomera3.modules.chat.messages.presentation.ITEM_TYPE_IMAGE_SEND
import com.numplates.nomera3.modules.chat.messages.presentation.ITEM_TYPE_ONLY_TEXT_RECEIVE
import com.numplates.nomera3.modules.chat.messages.presentation.ITEM_TYPE_ONLY_TEXT_SEND
import com.numplates.nomera3.modules.chat.messages.presentation.ITEM_TYPE_RECEIVE
import com.numplates.nomera3.modules.chat.messages.presentation.ITEM_TYPE_REPOST_RECEIVE
import com.numplates.nomera3.modules.chat.messages.presentation.ITEM_TYPE_REPOST_SEND
import com.numplates.nomera3.modules.chat.messages.presentation.ITEM_TYPE_SEND
import com.numplates.nomera3.modules.chat.messages.presentation.ITEM_TYPE_SHARE_COMMUNITY_RECEIVE
import com.numplates.nomera3.modules.chat.messages.presentation.ITEM_TYPE_SHARE_COMMUNITY_SEND
import com.numplates.nomera3.modules.chat.messages.presentation.ITEM_TYPE_SHARE_PROFILE_RECEIVE
import com.numplates.nomera3.modules.chat.messages.presentation.ITEM_TYPE_SHARE_PROFILE_SEND
import com.numplates.nomera3.modules.chat.messages.presentation.ITEM_TYPE_STICKER_RECEIVE
import com.numplates.nomera3.modules.chat.messages.presentation.ITEM_TYPE_STICKER_SEND
import com.numplates.nomera3.modules.chat.messages.presentation.ITEM_TYPE_VIDEO_RECEIVE
import com.numplates.nomera3.modules.chat.messages.presentation.ITEM_TYPE_VIDEO_SEND
import com.numplates.nomera3.modules.communities.data.entity.CommunityShareEntity
import java.util.Locale

private const val MESSAGE_LEFT_OFFSET = 8
private const val HEIGHT_REPLY_CONTAINER = 40
private const val MEERA_HEIGHT_REPLY_CONTAINER = 52
private const val APPEAR_DURATION = 250L

/**
 *
 * Use for reply menu under edittext
 *
 * */
class ReplyMenuHelper(private val root: ConstraintLayout?, private val onParentClicked: (MessageEntity) -> Unit) {

    val closeBtn: ImageView? = root?.findViewById(R.id.iv_close_reply)
    private val imageContainer: CardView? = root?.findViewById(R.id.cv_image_container)
    private val image: ImageView? = root?.findViewById(R.id.iv_reply_image)
    private val name: TextView? = root?.findViewById(R.id.tv_reply_name)
    private val message: TextView? = root?.findViewById(R.id.tv_message_txt)
    private val audioImage: ImageView? = root?.findViewById(R.id.iv_audio_message)
    var menuListener: (Boolean) -> Unit = { }

    init {
        closeBtn?.click {
            hideReplyContainer()
        }
    }

    fun showReply(replyEvent: ReplyEvent?, messageEntity: MessageEntity) {
        root?.setThrottledClickListener {
            onParentClicked(messageEntity)
        }
        when (replyEvent) {
            is ReplyEvent.ReplyImage -> {
                audioImage?.gone()
                imageContainer?.visible()
                image?.loadGlideCenterCrop(replyEvent.image)
                name?.text = replyEvent.name
                message?.setMargins(start = 8.dp)
                val title = if (replyEvent.image.isGifUrl()) {
                    root?.context?.getString(R.string.general_gif_uppercase)
                } else {
                    root?.context?.getString(R.string.profile_photo)
                }
                message?.setMargins(start = MESSAGE_LEFT_OFFSET.dp)
                message?.text = if (replyEvent.message.isNotEmpty()) {
                    replyEvent.message
                } else {
                    if (replyEvent.imageCount == 1 || replyEvent.imageCount == 0) title
                    else "${replyEvent.imageCount} ${title?.lowercase(Locale.getDefault())}"
                }
            }

            is ReplyEvent.ReplyVideo -> {
                audioImage?.gone()
                if (replyEvent.preview.isEmpty()) imageContainer?.gone()
                else imageContainer?.visible()
                image?.loadGlideCenterCrop(replyEvent.preview)
                name?.text = replyEvent.name
                message?.setMargins(start = MESSAGE_LEFT_OFFSET.dp)
                message?.text = if (replyEvent.message.isNotEmpty()) {
                    replyEvent.message
                } else {
                    root?.context?.getString(R.string.video) ?: ""
                }
            }

            is ReplyEvent.ReplyText -> {
                audioImage?.gone()
                imageContainer?.gone()
                name?.text = replyEvent.name
                message?.setMargins(start = MESSAGE_LEFT_OFFSET.dp)
                message?.text = replyEvent.message
            }

            is ReplyEvent.ReplyAudio -> {
                audioImage?.visible()
                imageContainer?.gone()
                name?.text = replyEvent.name
                message?.setMargins(start = 3.dp)
                message?.text = root?.context?.getString(R.string.audio_message)
            }
            //TODO https://nomera.atlassian.net/browse/BR-27361
            is ReplyEvent.ReplyPost -> {
                audioImage?.gone()
                imageContainer?.visible()
                image?.loadGlideCenterCrop(R.drawable.ic_repost_list_dark)
                name?.text = replyEvent.name
                message?.setMargins(start = MESSAGE_LEFT_OFFSET.dp)
                message?.text = if (replyEvent.isEvent) {
                    root?.context?.getString(R.string.map_events_configuration_title)
                } else if (replyEvent.isMoment){
                    replyEvent.message.takeIf { it.isNotEmpty() }
                        ?: root?.context?.getString(R.string.chat_repost_moment_message) ?: ""
                } else {
                    replyEvent.message.takeIf { it.isNotEmpty() }
                        ?: root?.context?.getString(R.string.post_title) ?: ""
                }
            }

            is ReplyEvent.ReplyMoment -> {
                audioImage?.gone()
                imageContainer?.visible()
                image?.loadGlide(R.drawable.ic_repost_list_dark)
                name?.text = replyEvent.name
                message?.setMargins(start = MESSAGE_LEFT_OFFSET.dp)
                message?.text = replyEvent.message.takeIf { it.isNotEmpty() }
                    ?: root?.context?.getString(R.string.moment_title)?: ""
            }

            is ReplyEvent.ReplyProfile -> {
                audioImage?.gone()
                imageContainer?.visible()
                image?.loadGlideSharedProfile(replyEvent)
                name?.text = replyEvent.name
                message?.setMargins(start = MESSAGE_LEFT_OFFSET.dp)
                message?.text = root?.context?.getString(R.string.profile_info) ?: String.empty()
            }

            is ReplyEvent.ReplyCommunity -> {
                audioImage?.gone()
                imageContainer?.visible()
                image?.loadGlideSharedCommunity(replyEvent)
                name?.text = replyEvent.name
                message?.setMargins(start = MESSAGE_LEFT_OFFSET.dp)
                message?.text = if (replyEvent.isPrivateCommunity)
                    root?.context?.getString(R.string.group_edit_fragment_close_option_name)
                else root?.context?.getString(R.string.group_edit_fragment_open_option_name)
            }

            is ReplyEvent.ReplySticker -> {
                audioImage?.gone()
                imageContainer?.visible()
                image?.visible()
                image?.loadGlideFitCenter(replyEvent.preview)
                name?.text = replyEvent.name
                message?.setMargins(start = MESSAGE_LEFT_OFFSET.dp)
                message?.text = root?.context?.getString(R.string.sticker)
            }

            else -> {
                message?.setMargins(start = MESSAGE_LEFT_OFFSET.dp)
                audioImage?.gone()
                hideReplyContainer()
                return
            }
        }
        showReplyContainer()
    }

    fun hideReply() {
        if (isMenuVisible()) hideReplyContainer()
    }

    private fun showReplyContainer() {
        root?.visible()
        val prevHeight = if ((root?.height ?: 0) > 0) root?.height ?: 0 else 1
        val newHeight = if (IS_APP_REDESIGNED) MEERA_HEIGHT_REPLY_CONTAINER else HEIGHT_REPLY_CONTAINER
        root?.animateHeight(prevHeight, newHeight.dp, APPEAR_DURATION) {
            menuListener(true)
        }
    }

    private fun hideReplyContainer() {
        root?.animateHeight(1, 250) {
            root.gone()
            menuListener(false)
        }
    }

    fun isMenuVisible() = root?.visibility == View.VISIBLE

}

/**
 *
 * Use for messages
 *
 * */
class ReplyMessageHelper(private val root: ViewGroup?) {

    private val imageContainer: CardView? = root?.findViewById(R.id.cv_image_container)
    private val image: ImageView? = root?.findViewById(R.id.iv_reply_image)
    private val name: TextView? = root?.findViewById(R.id.tv_reply_name)
    private val message: TextView? = root?.findViewById(R.id.tv_message_txt)
    private val audioImage: ImageView? = root?.findViewById(R.id.iv_audio_message)

    fun showReply(replyEvent: ReplyEvent?, isReceived: Boolean = false) {
        when (replyEvent) {
            is ReplyEvent.ReplyImage -> {
                audioImage?.gone()
                imageContainer?.visible()
                image?.loadGlideProgressive(replyEvent.image)
                name?.text = replyEvent.name
                val title = if (replyEvent.image.isGifUrl()) {
                    root?.context?.getString(R.string.general_gif_uppercase)
                } else {
                    root?.context?.getString(R.string.profile_photo)
                }
                message?.text = if (replyEvent.message.isNotEmpty()) {
                    replyEvent.message
                } else {
                    if (replyEvent.imageCount == 1 || replyEvent.imageCount == 0) title
                    else "${replyEvent.imageCount} ${title?.lowercase(Locale.getDefault())}"
                }
            }

            is ReplyEvent.ReplyVideo -> {
                audioImage?.gone()
                if (replyEvent.preview.isEmpty()) imageContainer?.gone()
                else imageContainer?.visible()
                image?.loadGlideProgressive(replyEvent.preview)
                name?.text = replyEvent.name
                message?.text = if (replyEvent.message.isNotEmpty()) {
                    replyEvent.message
                } else {
                    root?.context?.getString(R.string.video) ?: ""
                }
            }

            is ReplyEvent.ReplyText -> {
                audioImage?.gone()
                imageContainer?.gone()
                name?.text = replyEvent.name
                message?.text = replyEvent.message
            }

            is ReplyEvent.ReplyAudio -> {
                audioImage?.visible()
                imageContainer?.gone()
                name?.text = replyEvent.name
                message?.text = root?.context?.getString(R.string.audio_message)
            }

            //TODO https://nomera.atlassian.net/browse/BR-27361
            is ReplyEvent.ReplyPost -> {
                audioImage?.gone()
                imageContainer?.visible()
                if (isReceived) image?.loadGlide(R.drawable.ic_repost_list_dark)
                else image?.loadGlide(R.drawable.ic_repost_list_light)
                name?.text = replyEvent.name
                message?.text = if (replyEvent.isEvent) {
                    root?.context?.getString(R.string.map_events_configuration_title)
                }  else if (replyEvent.isMoment){
                    replyEvent.message.takeIf { it.isNotEmpty() }
                        ?: root?.context?.getString(R.string.chat_repost_moment_message) ?: ""
                } else {
                    replyEvent.message.takeIf { it.isNotEmpty() }
                        ?: root?.context?.getString(R.string.post_title) ?: ""
                }
            }

            is ReplyEvent.ReplyMoment -> {
                audioImage?.gone()
                imageContainer?.visible()
                if (isReceived) image?.loadGlide(R.drawable.ic_repost_list_dark)
                else image?.loadGlide(R.drawable.ic_repost_list_light)
                name?.text = replyEvent.name
                message?.text = replyEvent.message.takeIf { it.isNotEmpty() }
                    ?: root?.context?.getString(R.string.moment_title)?: ""
            }

            is ReplyEvent.ReplyProfile -> {
                audioImage?.gone()
                imageContainer?.visible()
                image?.loadGlideSharedProfile(replyEvent)
                name?.text = replyEvent.name
                message?.text = root?.context?.getString(R.string.profile_info) ?: String.empty()
            }

            is ReplyEvent.ReplyCommunity -> {
                audioImage?.gone()
                imageContainer?.visible()
                image?.loadGlideSharedCommunity(replyEvent)
                name?.text = replyEvent.name
                message?.text = if (replyEvent.isPrivateCommunity)
                    root?.context?.getString(R.string.group_edit_fragment_close_option_name)
                else root?.context?.getString(R.string.group_edit_fragment_open_option_name)
            }

            is ReplyEvent.ReplySticker -> {
                audioImage?.gone()
                imageContainer?.visible()
                image?.visible()
                image?.loadGlide(replyEvent.preview)
                name?.text = replyEvent.name
                message?.text = root?.context?.getString(R.string.sticker)
            }

            else -> {
                audioImage?.gone()
                root?.gone()
                return
            }
        }
        root?.visible()
    }

}

sealed class ReplyEvent {

    class ReplyImage(
        val name: String,
        val image: String,
        val message: String,
        val imageCount: Int
    ) : ReplyEvent()

    class ReplyText(
        val name: String,
        val message: String
    ) : ReplyEvent()

    class ReplyMapEvent(
        val name: String,
        val message: String
    ) : ReplyEvent()

    class ReplyAudio(
        val name: String
    ) : ReplyEvent()

    class ReplyPost(
        val name: String,
        val message: String,
        val isEvent: Boolean,
        val isMoment: Boolean
    ) : ReplyEvent()

    class ReplyMoment(
        val name: String,
        val message: String
    ) : ReplyEvent()

    class ReplyVideo(
        val name: String,
        val preview: String,
        val message: String,
    ) : ReplyEvent()

    class ReplyProfile(
        val name: String,
        val preview: String,
        val isProfileDeleted: Boolean
    ) : ReplyEvent()

    class ReplyCommunity(
        val name: String,
        val preview: String,
        val isPrivateCommunity: Boolean,
        val isCommunityDeleted: Boolean
    ) : ReplyEvent()

    class ReplySticker(
        val name: String,
        val preview: String
    ) : ReplyEvent()
}

fun MessageEntity.toReplyEvent(): ReplyEvent? {
    val messageText = tagSpan?.text ?: String.empty()
    when (itemType) {
        // Это изображение хз почему =(
        ITEM_TYPE_RECEIVE,
        ITEM_TYPE_IMAGE_RECEIVE,
        ITEM_TYPE_SEND,
        ITEM_TYPE_IMAGE_SEND -> {
            return ReplyEvent.ReplyImage(
                name = creator?.name ?: "",
                message = messageText,
                imageCount = attachments.size,
                image = attachments.firstOrNull()?.url ?: attachment.url
            )
        }

        ITEM_TYPE_AUDIO_RECEIVE,
        ITEM_TYPE_AUDIO_SEND -> {
            return ReplyEvent.ReplyAudio(
                name = creator?.name ?: ""
            )
        }

        ITEM_TYPE_REPOST_RECEIVE,
        ITEM_TYPE_REPOST_SEND -> {
            return ReplyEvent.ReplyPost(
                name = creator?.name ?: "",
                message = messageText,
                isEvent = attachment.type == CHAT_ITEM_TYPE_EVENT,
                isMoment = attachment.type == TYPING_TYPE_MOMENT
            )
        }

        ITEM_TYPE_ONLY_TEXT_RECEIVE,
        ITEM_TYPE_ONLY_TEXT_SEND -> {
            return ReplyEvent.ReplyText(
                name = creator?.name ?: "",
                message = messageText
            )
        }

        ITEM_TYPE_GREETING_RECEIVE,
        ITEM_TYPE_GREETING_SEND -> {
            return ReplyEvent.ReplyText(
                name = creator?.name ?: "",
                message = messageText
            )
        }

        ITEM_TYPE_VIDEO_RECEIVE,
        ITEM_TYPE_VIDEO_SEND -> {
            val preview = attachment.makeMetaMessageWithVideo()?.preview ?: ""
            return ReplyEvent.ReplyVideo(
                name = creator?.name ?: "",
                message = messageText,
                preview = preview
            )
        }

        ITEM_TYPE_SHARE_PROFILE_SEND,
        ITEM_TYPE_SHARE_PROFILE_RECEIVE -> {
            val user = Gson().fromJson<UserSimple?>(attachment.metadata)
            return ReplyEvent.ReplyProfile(
                name = creator?.name ?: String.empty(),
                preview = user?.avatarSmall ?: String.empty(),
                isProfileDeleted = user?.profileDeleted == 1
            )
        }

        ITEM_TYPE_SHARE_COMMUNITY_SEND,
        ITEM_TYPE_SHARE_COMMUNITY_RECEIVE -> {
            val community = Gson().fromJson<CommunityShareEntity?>(attachment.metadata)
            return ReplyEvent.ReplyCommunity(
                name = creator?.name ?: String.empty(),
                preview = community?.avatar ?: String.empty(),
                isPrivateCommunity = community?.private == 1,
                isCommunityDeleted = community?.deleted == 1
            )
        }

        ITEM_TYPE_STICKER_SEND,
        ITEM_TYPE_STICKER_RECEIVE -> {
            val preview = attachment.url
            return ReplyEvent.ReplySticker(
                name = creator?.name ?: String.empty(),
                preview = preview
            )
        }

        else -> {
            return null
        }
    }
}


fun ParentMessage.toReplyEvent(): ReplyEvent? {
    val messageText = messageContent
    when (type) {
        // Это изображение хз почему =(
        ITEM_TYPE_RECEIVE,
        ITEM_TYPE_IMAGE_RECEIVE,
        ITEM_TYPE_SEND,
        ITEM_TYPE_IMAGE_SEND -> {
            return ReplyEvent.ReplyImage(
                name = creatorName,
                message = messageText,
                imageCount = imageCount,
                image = imagePreview
            )
        }

        ITEM_TYPE_AUDIO_RECEIVE,
        ITEM_TYPE_AUDIO_SEND -> {
            return ReplyEvent.ReplyAudio(
                name = creatorName
            )
        }

        ITEM_TYPE_REPOST_RECEIVE,
        ITEM_TYPE_REPOST_SEND -> {
            return ReplyEvent.ReplyPost(
                name = creatorName,
                message = messageText,
                isEvent = isEvent,
                isMoment = isMoment
            )
        }

        ITEM_TYPE_GREETING_RECEIVE,
        ITEM_TYPE_GREETING_SEND -> {
            return ReplyEvent.ReplyText(
                name = creatorName,
                message = messageText
            )
        }

        ITEM_TYPE_ONLY_TEXT_RECEIVE,
        ITEM_TYPE_ONLY_TEXT_SEND -> {
            return ReplyEvent.ReplyText(
                name = creatorName,
                message = messageText
            )
        }

        ITEM_TYPE_VIDEO_RECEIVE,
        ITEM_TYPE_VIDEO_SEND -> {
            val preview = videoPreview
            return ReplyEvent.ReplyVideo(
                name = creatorName,
                message = messageText,
                preview = preview
            )
        }

        ITEM_TYPE_SHARE_PROFILE_SEND,
        ITEM_TYPE_SHARE_PROFILE_RECEIVE -> {
            return ReplyEvent.ReplyProfile(
                name = creatorName,
                preview = sharedProfileUrl,
                isProfileDeleted = isDeletedSharedProfile
            )
        }

        ITEM_TYPE_SHARE_COMMUNITY_SEND,
        ITEM_TYPE_SHARE_COMMUNITY_RECEIVE -> {
            return ReplyEvent.ReplyCommunity(
                name = creatorName,
                preview = sharedCommunityUrl,
                isPrivateCommunity = isPrivateCommunity,
                isCommunityDeleted = isDeletedSharedCommunity
            )
        }

        ITEM_TYPE_STICKER_SEND,
        ITEM_TYPE_STICKER_RECEIVE -> {
            return ReplyEvent.ReplySticker(
                name = creatorName,
                preview = imagePreview
            )
        }

        else -> {
            return null
        }
    }
}

fun MessageEntity.isValidForReply(): Boolean {
    return when (itemType) {
        // Send message
        ITEM_TYPE_SEND,
        ITEM_TYPE_IMAGE_SEND,
        ITEM_TYPE_AUDIO_SEND,
        ITEM_TYPE_REPOST_SEND,
        ITEM_TYPE_ONLY_TEXT_SEND,
        ITEM_TYPE_SHARE_PROFILE_SEND,
        ITEM_TYPE_SHARE_COMMUNITY_SEND,
        ITEM_TYPE_GREETING_SEND,
        ITEM_TYPE_VIDEO_SEND,
        ITEM_TYPE_STICKER_SEND -> true
        // Receive message
        ITEM_TYPE_RECEIVE,
        ITEM_TYPE_IMAGE_RECEIVE,
        ITEM_TYPE_AUDIO_RECEIVE,
        ITEM_TYPE_REPOST_RECEIVE,
        ITEM_TYPE_ONLY_TEXT_RECEIVE,
        ITEM_TYPE_SHARE_PROFILE_RECEIVE,
        ITEM_TYPE_SHARE_COMMUNITY_RECEIVE,
        ITEM_TYPE_GREETING_RECEIVE,
        ITEM_TYPE_VIDEO_RECEIVE,
        ITEM_TYPE_STICKER_RECEIVE -> true

        else -> false
    }
}

private fun ImageView.loadGlideSharedProfile(evt: ReplyEvent.ReplyProfile) {
    val avatar = if (evt.isProfileDeleted) R.drawable.chat_profile_deleted else {
        if (evt.preview.isEmpty()) R.drawable.fill_8 else evt.preview
    }
    Glide.with(this.context)
        .load(avatar)
        .centerCrop()
        .transition(DrawableTransitionOptions.withCrossFade())
        .into(this)
}

private fun ImageView.loadGlideSharedCommunity(evt: ReplyEvent.ReplyCommunity) {
    val avatar = if (evt.isCommunityDeleted) R.drawable.chat_profile_deleted else {
        if (evt.preview.isEmpty()) R.drawable.chat_profile_deleted else evt.preview
    }
    Glide.with(this.context)
        .load(avatar)
        .centerCrop()
        .transition(DrawableTransitionOptions.withCrossFade())
        .into(this)
}
