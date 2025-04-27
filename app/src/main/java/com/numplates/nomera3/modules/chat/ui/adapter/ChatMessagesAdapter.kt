package com.numplates.nomera3.modules.chat.ui.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.AnimationDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.format.DateFormat
import android.text.style.AbsoluteSizeSpan
import android.util.Size
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.animation.AnimationUtils
import android.view.animation.OvershootInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.core.view.doOnPreDraw
import androidx.core.view.isVisible
import androidx.core.view.marginBottom
import androidx.core.view.marginEnd
import androidx.core.view.marginStart
import androidx.core.view.marginTop
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.GranularRoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.google.gson.Gson
import com.google.gson.internal.LinkedTreeMap
import com.meera.core.extensions.click
import com.meera.core.extensions.color
import com.meera.core.extensions.doDelayed
import com.meera.core.extensions.dp
import com.meera.core.extensions.dpToPx
import com.meera.core.extensions.empty
import com.meera.core.extensions.fromJson
import com.meera.core.extensions.gone
import com.meera.core.extensions.inflate
import com.meera.core.extensions.invisible
import com.meera.core.extensions.loadGlide
import com.meera.core.extensions.loadGlideCircle
import com.meera.core.extensions.loadGlideCircleWithPlaceHolder
import com.meera.core.extensions.loadGlideGifWithCallback
import com.meera.core.extensions.loadGlideWithCacheAndError
import com.meera.core.extensions.longClick
import com.meera.core.extensions.setImageDrawable
import com.meera.core.extensions.setMargins
import com.meera.core.extensions.toBoolean
import com.meera.core.extensions.updatePadding
import com.meera.core.extensions.visible
import com.meera.core.utils.ApprovedIconSize
import com.meera.core.utils.EmojiUtils
import com.meera.core.utils.TopAuthorApprovedUserModel
import com.meera.core.utils.blur.BlurHelper
import com.meera.core.utils.enableTopContentAuthorApprovedUser
import com.meera.core.utils.getAge
import com.meera.core.utils.getDurationSeconds
import com.meera.core.utils.getShortTime
import com.meera.core.utils.layouts.ExpandableLayout
import com.meera.core.utils.timeAgoChat
import com.meera.db.models.dialog.DialogEntity
import com.meera.db.models.dialog.UserChat
import com.meera.db.models.message.MessageAttachment
import com.meera.db.models.message.MessageEntity
import com.meera.db.models.userprofile.UserSimple
import com.numplates.nomera3.CHAT_VOICE_MESSAGES_PATH
import com.numplates.nomera3.R
import com.numplates.nomera3.USER_GENDER_MALE
import com.numplates.nomera3.data.network.MediaAssetDto
import com.numplates.nomera3.data.network.Post
import com.numplates.nomera3.data.newmessenger.ROOM_TYPE_GROUP
import com.numplates.nomera3.data.newmessenger.TYPING_TYPE_GIF
import com.numplates.nomera3.data.newmessenger.TYPING_TYPE_IMAGE
import com.numplates.nomera3.databinding.ItemChatGreetingReceiveBinding
import com.numplates.nomera3.databinding.ItemChatGreetingSendBinding
import com.numplates.nomera3.databinding.ItemChatStickerReceiveBinding
import com.numplates.nomera3.databinding.ItemChatStickerSendBinding
import com.numplates.nomera3.databinding.ItemTypeDateDividerBinding
import com.numplates.nomera3.modules.baseCore.AccountTypeEnum
import com.numplates.nomera3.modules.chat.IOnMessageClickedNew
import com.numplates.nomera3.modules.chat.data.ChatBirthdayUiEntity
import com.numplates.nomera3.modules.chat.helpers.NoScrollableGridLayoutManager
import com.numplates.nomera3.modules.chat.helpers.RecognizedVoiceTextActionHelper
import com.numplates.nomera3.modules.chat.helpers.ReplyMessageHelper
import com.numplates.nomera3.modules.chat.helpers.isEdited
import com.numplates.nomera3.modules.chat.helpers.isFileByLocalPathExists
import com.numplates.nomera3.modules.chat.helpers.replymessage.ISwipeableHolder
import com.numplates.nomera3.modules.chat.helpers.toReplyEvent
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
import com.numplates.nomera3.modules.chat.messages.presentation.ITEM_TYPE_NO_MEDIA_PLACEHOLDER_SEND
import com.numplates.nomera3.modules.chat.messages.presentation.ITEM_TYPE_ONLY_TEXT_RECEIVE
import com.numplates.nomera3.modules.chat.messages.presentation.ITEM_TYPE_ONLY_TEXT_SEND
import com.numplates.nomera3.modules.chat.messages.presentation.ITEM_TYPE_RECEIVE
import com.numplates.nomera3.modules.chat.messages.presentation.ITEM_TYPE_REPOST_RECEIVE
import com.numplates.nomera3.modules.chat.messages.presentation.ITEM_TYPE_REPOST_SEND
import com.numplates.nomera3.modules.chat.messages.presentation.ITEM_TYPE_SEND
import com.numplates.nomera3.modules.chat.messages.presentation.ITEM_TYPE_SERVICE_MESSAGE
import com.numplates.nomera3.modules.chat.messages.presentation.ITEM_TYPE_SHARE_COMMUNITY_RECEIVE
import com.numplates.nomera3.modules.chat.messages.presentation.ITEM_TYPE_SHARE_COMMUNITY_SEND
import com.numplates.nomera3.modules.chat.messages.presentation.ITEM_TYPE_SHARE_PROFILE_RECEIVE
import com.numplates.nomera3.modules.chat.messages.presentation.ITEM_TYPE_SHARE_PROFILE_SEND
import com.numplates.nomera3.modules.chat.messages.presentation.ITEM_TYPE_STICKER_RECEIVE
import com.numplates.nomera3.modules.chat.messages.presentation.ITEM_TYPE_STICKER_SEND
import com.numplates.nomera3.modules.chat.messages.presentation.ITEM_TYPE_VIDEO_RECEIVE
import com.numplates.nomera3.modules.chat.messages.presentation.ITEM_TYPE_VIDEO_SEND
import com.numplates.nomera3.modules.chat.ui.mapper.ChatMessageEventLabelUiMapper
import com.numplates.nomera3.modules.communities.data.entity.CommunityShareEntity
import com.numplates.nomera3.modules.feed.ui.entity.PostTypeEnum
import com.numplates.nomera3.modules.maps.data.model.EventDto
import com.numplates.nomera3.modules.maps.ui.events.EventChipsView
import com.numplates.nomera3.modules.maps.ui.events.mapper.EventsCommonUiMapper
import com.numplates.nomera3.modules.maps.ui.events.model.EventChipsType
import com.numplates.nomera3.modules.maps.ui.events.model.EventChipsUiModel
import com.numplates.nomera3.modules.moments.show.data.MomentItemDto
import com.numplates.nomera3.modules.newroads.data.entities.MediaEntity
import com.numplates.nomera3.modules.search.ui.fragment.AT_SIGN
import com.numplates.nomera3.modules.tags.data.entity.SpanDataClickType
import com.numplates.nomera3.presentation.audio.VisualizerVoiceView
import com.numplates.nomera3.presentation.audio.VoiceMessageView
import com.numplates.nomera3.presentation.model.enums.CallStatusEnum
import com.numplates.nomera3.presentation.model.enums.ChatEventEnum
import com.numplates.nomera3.presentation.utils.TEXT_SPACE_DEFAULT_RECEIVE_12H
import com.numplates.nomera3.presentation.utils.TEXT_SPACE_DEFAULT_RECEIVE_24H
import com.numplates.nomera3.presentation.utils.TEXT_SPACE_DEFAULT_SEND_12H
import com.numplates.nomera3.presentation.utils.TEXT_SPACE_DEFAULT_SEND_24H
import com.numplates.nomera3.presentation.utils.spanTagsChatText
import com.numplates.nomera3.presentation.view.adapter.newchat.chatimage.ChatImagesAdapter
import com.numplates.nomera3.presentation.view.adapter.newchat.chatimage.ChatImagesAdapter.Companion.MAX_ROWS
import com.numplates.nomera3.presentation.view.adapter.newchat.chatimage.PostImage
import com.numplates.nomera3.presentation.view.adapter.newuserprofile.NOOMEERA_ACCOUNT_ID
import com.numplates.nomera3.presentation.view.adapter.newuserprofile.TYPE_GIFT_HOLIDAY
import com.numplates.nomera3.presentation.view.adapter.newuserprofile.TYPE_GIFT_HOLIDAY_NEW_YEAR
import com.numplates.nomera3.presentation.view.ui.TextViewWithImages
import com.numplates.nomera3.presentation.view.utils.NTime
import com.numplates.nomera3.presentation.view.utils.NTime.Companion.timeAgo
import com.numplates.nomera3.presentation.view.utils.zoomy.Zoomy
import com.numplates.nomera3.presentation.view.widgets.DetectorSeekBar
import timber.log.Timber
import java.io.File
import java.lang.Integer.max
import java.lang.Integer.min

private const val ENTER_FADE_DURATION_MS = 300
private const val EXIT_FADE_DURATION_MS = 1100
private const val DISABLE_SCROLL_DELAY_MS = 2000L
private const val META_DATA_TYPE_ID = "type_id"
private const val META_DATA_CUSTOM_TITLE = "custom_title"
private const val META_DATA_POST = "post"
private const val META_DATA_MOMENT = "moment"
private const val MEDIA_RECYCLER_WIDTH_OFFSET = 22
private const val HORIZONTAL_MEDIA_RANGE_SIZE = 5

class ChatMessagesAdapter(
    private val act: Activity,
    private val myUid: Long,
    private var room: DialogEntity?,
    private val onMessageClicked: IOnMessageClickedNew,
    private val isMessageEditEnabled: Boolean,
    private val screenWidth: Int,
    private val blurHelper: BlurHelper,
    private val isHiddenAgeAndGender: Boolean
) : RecyclerView.Adapter<ChatMessagesAdapter.BaseMessageViewHolder>() {

    private val roomType: String? = room?.type

    private val gson = Gson()

    var isEventsEnabled = true
    var isSomeBodyHasBirthday = false
    var isRoomBlocked = false

    private var highlightBackground: Pair<String?, Int> = Pair(null, RecyclerView.NO_POSITION)
    private val chatMessageEventLabelUiMapper = ChatMessageEventLabelUiMapper(EventsCommonUiMapper(act))
    private val asyncListDiffer = ModifyAsyncListDiffer(this, diffCallback)
    private var messageWithUnreadDivider: MessageEntity? = null
    private val maxBubbleWidth
        get() = (screenWidth * MESSAGE_WIDTH_RELATIVE).toInt() - dpToPx(MESSAGE_SIDE_MARGIN)

    override fun getItemViewType(position: Int): Int {
        val item = getItem(position)
        return getMessageType(item)
    }

    override fun getItemCount() = asyncListDiffer.currentList.size

    private fun getItem(position: Int): MessageEntity? = asyncListDiffer.currentList[position]

    fun refreshRoomData(room: DialogEntity?) {
        this.room = room
    }

    fun submitList(
        newList: List<MessageEntity>,
        callback: Runnable = Runnable {}
    ) = asyncListDiffer.submitList(newList, callback)

    fun getCurrentList(): List<MessageEntity> = asyncListDiffer.currentList

    fun highlightMessageBackground(messageId: String, position: Int) {
        highlightBackground = Pair(messageId, position)
        notifyItemChanged(position)
    }

    override fun getItemId(position: Int): Long {
        return getMessageItem(position)?.msgId?.toLong() ?: 0L
    }

    fun getMessageItem(position: Int): MessageEntity? {
        return try {
            getItem(position)
        } catch (e: Exception) {
            Timber.e("Get message item error ${e.message}")
            null
        }
    }

    fun addMessageToEnd(message: MessageEntity, commitCallback: () -> Unit) {
        asyncListDiffer.fastInsertEnd(message, commitCallback)
    }

    private fun getMessageType(message: MessageEntity?): Int = message?.itemType ?: -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseMessageViewHolder =
        when (viewType) {
            ITEM_TYPE_SEND -> SenderMessageHolder(
                parent.inflate(R.layout.item_chat_message_sender),
                onMessageClicked
            )
            ITEM_TYPE_RECEIVE -> ReceiverMessageHolder(
                parent.inflate(R.layout.item_chat_message_receiver),
                onMessageClicked
            )
            ITEM_TYPE_IMAGE_SEND -> SenderImageHolder(
                parent.inflate(R.layout.item_type_image_sender),
                onMessageClicked
            )
            ITEM_TYPE_IMAGE_RECEIVE -> ReceiverImageHolder(
                parent.inflate(R.layout.item_type_image_receiver),
                onMessageClicked
            )
            ITEM_TYPE_AUDIO_SEND -> SenderVoiceMessageHolder(
                parent.inflate(R.layout.item_chat_audio_sender),
                onMessageClicked
            )
            ITEM_TYPE_AUDIO_RECEIVE -> ReceiverVoiceMessageHolder(
                parent.inflate(R.layout.item_chat_audio_receiver),
                onMessageClicked
            )
            ITEM_TYPE_SERVICE_MESSAGE -> ServiceMessageHolder(
                parent.inflate(R.layout.item_chat_service_message)
            )
            ITEM_TYPE_CALLS -> CallMessageViewHolder(
                parent.inflate(R.layout.item_chat_message_calls),
                myUid,
                onMessageClicked
            )
            ITEM_TYPE_MESSAGE_SEND_DELETED -> SendMessageDeletedHolder(
                parent.inflate(R.layout.item_chat_message_sender_deleted),
                onMessageClicked
            )
            ITEM_TYPE_MESSAGE_RECEIVE_DELETED -> ReceiveMessageDeletedHolder(
                parent.inflate(R.layout.item_chat_message_receiver_deleted),
                onMessageClicked
            )
            ITEM_TYPE_REPOST_SEND -> SenderRepostHolder(parent.inflate(R.layout.item_chat_repost_send))
            ITEM_TYPE_REPOST_RECEIVE -> ReceiverRepostHolder(parent.inflate(R.layout.item_chat_repost_receive))
            ITEM_TYPE_ONLY_TEXT_SEND -> SenderTextMessage(
                parent.inflate(R.layout.item_chat_text_message_sender),
                onMessageClicked
            )
            ITEM_TYPE_ONLY_TEXT_RECEIVE -> ReceiveTextMessage(
                parent.inflate(R.layout.item_chat_text_message_receive),
                onMessageClicked
            )
            ITEM_TYPE_GIFT_SEND -> SenderGiftViewHolder(
                parent.inflate(R.layout.item_chat_gift_sender),
                onMessageClicked
            )
            ITEM_TYPE_GIFT_RECEIVE -> ReceiverGiftViewHolder(
                parent.inflate(R.layout.item_chat_gift_receiver),
                onMessageClicked
            )
            ITEM_TYPE_VIDEO_SEND -> SenderVideoViewHolder(parent.inflate(R.layout.item_type_video_sender))
            ITEM_TYPE_VIDEO_RECEIVE -> ReceiverVideoViewHolder(
                parent.inflate(R.layout.item_type_video_receiver),
                onMessageClicked
            )
            ITEM_TYPE_SHARE_PROFILE_SEND -> SenderShareProfileViewHolder(
                parent.inflate(R.layout.item_type_share_profile_sender)
            )
            ITEM_TYPE_SHARE_PROFILE_RECEIVE -> ReceiverShareProfileViewHolder(
                parent.inflate(R.layout.item_type_share_profile_receiver)
            )
            ITEM_TYPE_SHARE_COMMUNITY_SEND -> SenderShareCommunityViewHolder(
                parent.inflate(R.layout.item_type_share_community_sender)
            )
            ITEM_TYPE_SHARE_COMMUNITY_RECEIVE -> ReceiverShareCommunityViewHolder(
                parent.inflate(R.layout.item_type_share_community_receiver)
            )
            ITEM_TYPE_NO_MEDIA_PLACEHOLDER_SEND -> NoMediaViewHolder(
                parent.inflate(R.layout.item_type_no_media_placeholder_send)
            )
            ITEM_TYPE_GREETING_RECEIVE -> {
                val inflater = LayoutInflater.from(parent.context)
                val binding = ItemChatGreetingReceiveBinding.inflate(inflater, parent, false)
                GreetingReceiveViewHolder(
                    binding,
                    onMessageClicked
                )
            }
            ITEM_TYPE_GREETING_SEND -> {
                val inflater = LayoutInflater.from(parent.context)
                val binding = ItemChatGreetingSendBinding.inflate(inflater, parent, false)
                GreetingSendViewHolder(
                    binding,
                    onMessageClicked
                )
            }
            ITEM_TYPE_DATE_DIVIDER -> {
                val inflater = LayoutInflater.from(parent.context)
                val binding = ItemTypeDateDividerBinding.inflate(inflater, parent, false)
                DateDividerViewHolder(binding)
            }
            ITEM_TYPE_STICKER_SEND -> {
                val inflater = LayoutInflater.from(parent.context)
                val binding = ItemChatStickerSendBinding.inflate(inflater, parent, false)
                SenderStickerHolder(binding, onMessageClicked)
            }
            ITEM_TYPE_STICKER_RECEIVE -> {
                val inflater = LayoutInflater.from(parent.context)
                val binding = ItemChatStickerReceiveBinding.inflate(inflater, parent, false)
                ReceiverStickerHolder(binding, onMessageClicked)
            }
            else -> EmptyViewHolder(parent.inflate(R.layout.item_chat_empty_view))
        }

    override fun onBindViewHolder(holder: BaseMessageViewHolder, position: Int) {
        val item = getItem(position)
        val previousMessage = if (position < itemCount - 1) getItem(position + 1) else null
        val nextMessage = if (position > 0) getItem(position - 1) else null
        when (getItemViewType(position)) {
            ITEM_TYPE_SEND -> (holder as SenderMessageHolder).bind(item, previousMessage)
            ITEM_TYPE_RECEIVE -> (holder as ReceiverMessageHolder).bind(item, roomType, previousMessage, nextMessage)
            ITEM_TYPE_IMAGE_SEND -> (holder as SenderImageHolder).bind(item, previousMessage)
            ITEM_TYPE_IMAGE_RECEIVE -> (holder as ReceiverImageHolder).bind(item, roomType, previousMessage)
            ITEM_TYPE_AUDIO_SEND -> (holder as SenderVoiceMessageHolder).bind(item, previousMessage)
            ITEM_TYPE_AUDIO_RECEIVE -> (holder as ReceiverVoiceMessageHolder).bind(
                item,
                roomType,
                previousMessage
            )
            ITEM_TYPE_SERVICE_MESSAGE -> (holder as ServiceMessageHolder).bind(item)
            ITEM_TYPE_CALLS -> (holder as CallMessageViewHolder).bind(item, previousMessage)
            ITEM_TYPE_MESSAGE_SEND_DELETED -> (holder as SendMessageDeletedHolder).bind(item, previousMessage)
            ITEM_TYPE_MESSAGE_RECEIVE_DELETED -> (holder as ReceiveMessageDeletedHolder).bind(
                item,
                previousMessage,
                roomType
            )
            ITEM_TYPE_REPOST_SEND -> (holder as SenderRepostHolder).bind(item, previousMessage)
            ITEM_TYPE_REPOST_RECEIVE -> (holder as ReceiverRepostHolder).bind(item, previousMessage)
            ITEM_TYPE_ONLY_TEXT_SEND -> (holder as SenderTextMessage).bind(item, previousMessage)
            ITEM_TYPE_ONLY_TEXT_RECEIVE -> (holder as ReceiveTextMessage).bind(item, previousMessage, nextMessage)
            ITEM_TYPE_GIFT_SEND -> (holder as SenderGiftViewHolder).bind(item, previousMessage)
            ITEM_TYPE_GIFT_RECEIVE -> (holder as ReceiverGiftViewHolder).bind(item, previousMessage)
            ITEM_TYPE_VIDEO_SEND -> (holder as SenderVideoViewHolder).bind(item, previousMessage)
            ITEM_TYPE_VIDEO_RECEIVE -> (holder as ReceiverVideoViewHolder).bind(item, roomType, previousMessage)
            ITEM_TYPE_SHARE_PROFILE_SEND -> (holder as SenderShareProfileViewHolder).bind(item, previousMessage)
            ITEM_TYPE_SHARE_PROFILE_RECEIVE -> (holder as ReceiverShareProfileViewHolder).bind(item, previousMessage)
            ITEM_TYPE_SHARE_COMMUNITY_SEND -> (holder as SenderShareCommunityViewHolder).bind(item, previousMessage)
            ITEM_TYPE_SHARE_COMMUNITY_RECEIVE -> (holder as ReceiverShareCommunityViewHolder).bind(
                item,
                previousMessage
            )
            ITEM_TYPE_NO_MEDIA_PLACEHOLDER_SEND -> (holder as NoMediaViewHolder).bind(item, previousMessage)
            ITEM_TYPE_GREETING_RECEIVE -> (holder as GreetingReceiveViewHolder).bind(item, previousMessage)
            ITEM_TYPE_GREETING_SEND -> (holder as GreetingSendViewHolder).bind(item, previousMessage)
            ITEM_TYPE_DATE_DIVIDER -> (holder as DateDividerViewHolder).bind(item)
            ITEM_TYPE_STICKER_SEND -> (holder as SenderStickerHolder).bind(item, previousMessage)
            ITEM_TYPE_STICKER_RECEIVE -> (holder as ReceiverStickerHolder).bind(item, previousMessage)
        }
    }

    override fun onBindViewHolder(holder: BaseMessageViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads)
            return
        }
        if (handleBindViewHoldersWithPayloadsIfPossible(holder, position, payloads)) {
            return
        }
        super.onBindViewHolder(holder, position, payloads)
    }

    private fun handleBindViewHoldersWithPayloadsIfPossible(
        holder: BaseMessageViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ): Boolean {
        val item = getItem(position)
        val itemViewType = getItemViewType(position)
        val diff = payloads[0] as Bundle
        var isDiffApplied = false
        diff.keySet().forEach { key ->
            if (key == PAYLOAD_VOICE_TEXT_NOW_RECOGNIZED) {
                when (itemViewType) {
                    ITEM_TYPE_AUDIO_RECEIVE -> (holder as ReceiverVoiceMessageHolder).bindRecognizedText(item)
                    ITEM_TYPE_AUDIO_SEND -> (holder as SenderVoiceMessageHolder).bindRecognizedText(item)
                }
                isDiffApplied = true
            }
            if (key == PAYLOAD_VOICE_REBIND) {
                when (itemViewType) {
                    ITEM_TYPE_AUDIO_RECEIVE -> (holder as ReceiverVoiceMessageHolder).bindPlayVoice(item)
                    ITEM_TYPE_AUDIO_SEND -> (holder as SenderVoiceMessageHolder).bindPlayVoice(item)
                }
                isDiffApplied = true
            }
            if (key == PAYLOAD_ATTACHMENT_URL) {
                isDiffApplied = true
            }
        }
        return isDiffApplied
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

    inner class DateDividerViewHolder(
        private val viewBinding: ItemTypeDateDividerBinding
    ): BaseMessageViewHolder(viewBinding.vgDateDivider) {

        fun bind(message: MessageEntity?) {
            if (message == null) return
            timeAgoChat(getContext(), message.createdAt).let {
                viewBinding.tvDateDividerDate.text = it
            }
            baseDividerContainer = viewBinding.vgDateDivider
            changeDividerStyle()
        }

        private fun getContext() = viewBinding.root.context
    }

    inner class SenderRepostHolder(view: View) :
        BaseMessageViewHolder(view),
        View.OnLongClickListener,
        ISwipeableHolder {

        private val messageLayout: LinearLayout = view.findViewById(R.id.layout_chat_message_repost_sender)

        private val ivRepostUserAvatar: ImageView = view.findViewById(R.id.iv_repost_user_avatar_sender)
        private val tvRepostUserName: TextView = view.findViewById(R.id.tv_repost_user_name_sender)
        private val tvRepostCreated: TextView = view.findViewById(R.id.tv_repost_created_sender)
        private val tvRepostImage: ImageView = view.findViewById(R.id.iv_repost_image_sender)
        private val tvRepostImageContainer: CardView = view.findViewById(R.id.cv_repost_image_sender_container)
        private val videoDurationContainer: View = view.findViewById(R.id.repost_image_sender_video_duration)
        private val cvRepostContainer: ConstraintLayout = view.findViewById(R.id.cl_repost_container_sender)
        private val clReplyContainer: ConstraintLayout =
            view.findViewById(R.id.ll_message_bubble_container_image_sender)

        private val tvPostDeleted: TextView = view.findViewById(R.id.iv_repost_post_deleted_sender)
        private val tvRepostText: TextViewWithImages = view.findViewById(R.id.tv_repost_text_sender)
        private val tvRepostTitle: TextViewWithImages = view.findViewById(R.id.tv_repost_title_sender)
        private val tvRepostType: TextView = view.findViewById(R.id.tv_repost_type_sender)
        private val tvShowMorePost: TextView = view.findViewById(R.id.tv_show_more_text_sender)

        private val repostParentType: TextView = view.findViewById(R.id.tv_post_repost_type_sender)

        // Bottom post + repost container
        private val postRepostContainer: LinearLayout = view.findViewById(R.id.post_repost_container_sender)
        private val tvPostRepostUserName: TextView = view.findViewById(R.id.tv_post_repost_user_name_sender)

        private val tvChatMessage: TextView = view.findViewById(R.id.tv_chat_msg_repost_send)
        private val tvChatMessageTime: TextView = view.findViewById(R.id.tv_chat_time_repost_send)

        private val ivMarkerSent: ImageView = view.findViewById(R.id.iv_marker_sent_repost_sender)
        private val ivMarkerDelivered: ImageView = view.findViewById(R.id.iv_marker_delivered_repost_sender)
        private val ivMarkerRead: ImageView = view.findViewById(R.id.iv_marker_read_repost_sender)

        private val bottomBlock: ConstraintLayout = view.findViewById(R.id.bottom_block)
        private val ivBluredContent: ImageView = view.findViewById(R.id.iv_blured_content)
        private val sensitiveChatSender: FrameLayout = view.findViewById(R.id.sensitive_chat_sender)
        private val cvShowPost: CardView = view.findViewById(R.id.cv_show_post)

        //music
        private val rootMusic: ConstraintLayout = view.findViewById(R.id.cl_root)
        private val ivMusicAlbum: ImageView = view.findViewById(R.id.iv_music_album)
        private val tvMusicArtist: TextView = view.findViewById(R.id.tv_music_artist)
        private val tvMusicName: TextView = view.findViewById(R.id.tv_music_title)

        private val fwdContainer: LinearLayout = view.findViewById(R.id.forward_container_send)
        private val tvFwdAuthorName: TextView = view.findViewById(R.id.tv_fwd_author_name)
        private val tvMessageEdited: TextView = view.findViewById(R.id.tv_message_edited)

        private val chatEventChips: EventChipsView = view.findViewById(R.id.ecv_event_chips)

        private val ivMultimediaView: View = view.findViewById(R.id.iv_repost_multimedia_view_sender)

        private var message: MessageEntity? = null

        override fun canSwipe() = isMessageSwipeEnabled(message)

        override fun getSwipeContainer() = clReplyContainer

        fun bind(message: MessageEntity?, prevMessage: MessageEntity?) {
            message?.let { currentMessage ->
                tvMessageEdited.isVisible = isMessageEditEnabled && currentMessage.isEdited()
                this.message = currentMessage
                handleEmojis(tvChatMessage, currentMessage)
                setMessageTime(tvChatMessageTime, currentMessage.createdAt)
                setMessageContainerTopMargin(
                    messageLayout,
                    currentMessage,
                    prevMessage
                )
                handleMessageStatus(
                    ivMarkerDelivered,
                    ivMarkerRead,
                    ivMarkerSent,
                    currentMessage.delivered,
                    currentMessage.readed,
                    currentMessage.sent
                )
                handleReplyMessage(currentMessage, onMessageClicked)
                highlightMessageScrollAnimation(true, currentMessage, clReplyContainer)
                cvRepostContainer.setOnLongClickListener(this)
                bottomBlock.setOnLongClickListener(this)
                if (currentMessage.content.isEmpty()) tvChatMessage.gone()
                else tvChatMessage.visible()
                val postMap = message.attachment.metadata[META_DATA_POST] as? LinkedTreeMap<String, Any>
                if (postMap == null) {
                    hideMusicContainer()
                    hideEventContainer()
                }
                postMap?.let {
                    val post = gson.fromJson<Post?>(postMap)
                    tvRepostType.setText(if (post?.event != null) {
                        R.string.chat_repost_event_user_title
                    } else {
                        R.string.chat_repost_user_title
                    })

                    post?.id?.let { postID ->
                        tvRepostUserName.setOnClickListener {
                            if (post.deleted == 1) return@setOnClickListener
                            onMessageClicked.onShowMoreRepost(postID)
                        }
                        ivRepostUserAvatar.setOnClickListener {
                            if (post.deleted == 1) return@setOnClickListener
                            onMessageClicked.onShowMoreRepost(postID)
                        }
                        rootMusic.setOnClickListener {
                            if (post.deleted == 1) return@setOnClickListener
                            onMessageClicked.onShowMoreRepost(postID)
                        }
                        chatEventChips.setOnClickListener {
                            if (post.deleted == 1) return@setOnClickListener
                            onMessageClicked.onShowMoreRepost(postID)
                        }
                        tvRepostUserName.setOnLongClickListener(this)
                        ivRepostUserAvatar.setOnLongClickListener(this)
                        rootMusic.setOnLongClickListener(this)
                    }
                    val assets = post?.assets
                    val media = if (!assets.isNullOrEmpty()) {
                        assets[0]
                    } else null

                    val img: String? = when {
                        post?.asset?.metadata?.preview != null -> post.asset?.metadata?.preview
                        post?.asset?.url != null -> post.asset?.url
                        media != null -> getUrlFromMedia(media)
                        else -> null
                    }
                    if (post?.postType == PostTypeEnum.AVATAR_VISIBLE.value ||
                        post?.postType == PostTypeEnum.AVATAR_HIDDEN.value) {
                        tvRepostImage.setPadding(dpToPx(24), dpToPx(24), dpToPx(24), dpToPx(24))
                        tvRepostImage.loadGlideCircle(img)
                    } else {
                        tvRepostImage.setPadding(0, 0, 0, 0)
                        tvRepostImage.loadGlide(img)
                    }
                    val aspect = media?.metadata?.aspect ?: post?.asset?.metadata?.aspect
                    setupAspect(tvRepostImage, aspect)
                    tvRepostImageContainer.setMargins(
                        tvRepostImageContainer.marginStart, tvRepostImageContainer.marginTop, 0, tvRepostImageContainer.marginBottom)

                    val videoDuration = media?.metadata?.duration ?: post?.asset?.metadata?.duration
                    videoDuration?.let { duration ->
                        val durationText = getDurationSeconds(duration)
                        videoDurationContainer.visible()
                        val time = videoDurationContainer.findViewById<TextView>(R.id.exo_position)
                        time.text = durationText
                    } ?: let {
                        videoDurationContainer.gone()
                    }

                    if (post?.deleted == 1) {
                        tvPostDeleted.visible()
                        tvPostDeleted.text = itemView.resources.getString(R.string.chat_repost_post_deleted)
                        tvRepostImage.gone()
                        videoDurationContainer.gone()
                        tvRepostText.gone()
                        tvRepostTitle.gone()
                    } else {
                        tvPostDeleted.gone()
                        tvRepostImage.visible()
                        tvRepostText.visible()
                        tvRepostTitle.visible()
                        post?.id?.let { postID ->
                            tvRepostImage.setOnClickListener { onMessageClicked.onShowMoreRepost(postID) }
                            tvRepostText.setOnClickListener { onMessageClicked.onShowMoreRepost(postID) }
                            tvRepostTitle.setOnClickListener { onMessageClicked.onShowMoreRepost(postID) }
                            tvRepostImage.setOnLongClickListener(this)
                            tvRepostText.setOnLongClickListener(this)
                            tvRepostTitle.setOnLongClickListener(this)
                        }
                    }

                    tvRepostUserName.handlePostAuthorStatuses(post)
                    tvRepostUserName.text = post?.user?.name ?: ""
                    ivRepostUserAvatar.loadGlideCircleWithPlaceHolder(post?.user?.avatarSmall, R.drawable.fill_8_round)

                    if (post?.text.isNullOrEmpty()) tvRepostText.gone()
                    else tvRepostText.visible()

                    trimPostLength(tvRepostText, tvShowMorePost, true, post?.id)
                    handleRepostTextUniqueNames(post, tvRepostText, R.color.ui_color_chat_send_grey)

                    if (post?.event != null && post.event?.title.isNullOrEmpty().not() && isEventsEnabled) {
                        handleRepostTitleUniqueNames(post, tvRepostTitle, R.color.ui_color_chat_send_grey)
                        tvRepostTitle.visible()
                    } else {
                        tvRepostTitle.gone()
                    }

                    if (post?.parentPost == null) {
                        postRepostContainer.gone()
                    } else {
                        postRepostContainer.visible()
                        post.parentPost?.let { parentPost ->
                            repostParentType.setText(
                                if (parentPost.event != null) R.string.event else R.string.post
                            )
                            tvPostRepostUserName.text = parentPost.user?.name ?: ""
                            postRepostContainer.setOnClickListener {
                                if (post.deleted == 0)
                                    onMessageClicked.onShowMoreRepost(post.id)
                            }
                        }
                    }

                    post?.createdAt?.let { dataPost ->
                        tvRepostCreated.text = timeAgo(dataPost / 1000)
                    }

                    setupBlur(post, img, post?.deleted.toBoolean())
                    setupMusicContainer(post?.mediaEntity)
                    setupEventContainer(
                        event = post?.event,
                        postDeleted = post?.deleted.toBoolean()
                    )
                    setupMultimediaView(post)

                    handleMessageForwarding(currentMessage, fwdContainer, tvFwdAuthorName, onMessageClicked)
                }

                setupContentForMoment(currentMessage)
            }
        }

        /**
         * Техдолг: https://nomera.atlassian.net/browse/BR-17753
         * Необходимо выпилить Dto из адаптеров
         */
        private fun setupContentForMoment(currentMessage: MessageEntity) {
            @Suppress("UNCHECKED_CAST")
            val momentMap = currentMessage.attachment.metadata[META_DATA_MOMENT] as? LinkedTreeMap<String, Any> ?: return
            val moment = gson.fromJson<MomentItemDto?>(momentMap) ?: return
            tvRepostType.text = itemView.resources.getString(R.string.chat_repost_moment_title)
            moment.setContentVisibilityForMoment()
            if (moment.deleted.toBoolean() || !moment.active.toBoolean()) {
                tvPostDeleted.text = itemView.resources.getString(R.string.chat_repost_moment_unavailable)
            } else {
                tvRepostImage.setOnClickListener { onMessageClicked.onShowRepostMoment(moment.id) }
                tvRepostUserName.setOnClickListener { onMessageClicked.onShowRepostMoment(moment.id) }
                ivRepostUserAvatar.setOnClickListener { onMessageClicked.onShowRepostMoment(moment.id) }
            }
            tvRepostImageContainer.setMargins(
                tvRepostImageContainer.marginStart, tvRepostImageContainer.marginTop, 8.dp, tvRepostImageContainer.marginBottom)
            moment.asset?.preview?.let { tvRepostImage.loadGlide(it) }
            setupAspect(view = tvRepostImage, aspect = null)
            tvRepostUserName.text = moment.user?.name ?: ""
            ivRepostUserAvatar.loadGlideCircleWithPlaceHolder(moment.user?.avatarSmall, R.drawable.fill_8_round)
            tvRepostCreated.text = NTime.timeAgo(date = moment.createdAt, shouldTrimAgo = true)
            handleMessageForwarding(
                message = currentMessage,
                container = fwdContainer,
                tvName = tvFwdAuthorName,
                onMessageClicked = onMessageClicked
            )
        }

        private fun MomentItemDto.setContentVisibilityForMoment() {
            val isMomentUnavailable = deleted.toBoolean() || !active.toBoolean()
            tvPostDeleted.isVisible = isMomentUnavailable
            tvRepostImage.isVisible = !isMomentUnavailable
            tvRepostTitle.gone()
            tvShowMorePost.gone()
            videoDurationContainer.gone()
            tvRepostText.gone()
            postRepostContainer.gone()
            sensitiveChatSender.gone()
            cvShowPost.gone()
            hideMusicContainer()
        }

        override fun onLongClick(v: View?): Boolean {
            onMessageClicked.onMessageLongClicked(message, bottomBlock)
            return true
        }

        private fun setupMusicContainer(media: MediaEntity?) {
            media?.let {
                rootMusic.visible()
                tvMusicArtist.text = media.artist
                tvMusicName.setTextColor(Color.WHITE)
                tvMusicArtist.setTextColor(ContextCompat.getColor(itemView.context, R.color.gray_separator))
                tvMusicName.text = media.track
                ivMusicAlbum.loadGlide(media.albumUrl)
            } ?: kotlin.run {
                hideMusicContainer()
            }
        }

        private fun setupEventContainer(event: EventDto?, postDeleted: Boolean) {
            if (event != null && postDeleted.not() && isEventsEnabled) {
                val chipsUiModel = EventChipsUiModel(
                    type = EventChipsType.LIGHT,
                    label = chatMessageEventLabelUiMapper.mapEventLabelUiModel(
                        eventDto = event,
                        isSenderMessage = true
                    )
                )
                chatEventChips.visible()
                chatEventChips.setModel(chipsUiModel)
            } else {
                hideEventContainer()
            }
        }

        private fun hideMusicContainer() {
            rootMusic.gone()
        }

        private fun hideEventContainer() {
            chatEventChips.gone()
        }

        private fun setupAspect(view: View, aspect: Float?) {
            Timber.d("setupAspect aspect = $aspect")
            view.post {
                aspect?.let {
                    if (aspect > 0.toDouble()) {
                        val layoutParams = view.layoutParams
                        layoutParams?.width = view.width
                        layoutParams?.height = (view.width / aspect).toInt()
                        view.layoutParams = layoutParams
                    }
                } ?: kotlin.run {
                    val layoutParams = view.layoutParams
                    layoutParams?.width = MATCH_PARENT
                    layoutParams?.height = WRAP_CONTENT
                    view.layoutParams = layoutParams
                }
            }
        }

        private fun setupBlur(post: Post?, img: String?, isPostDeleted: Boolean) {
            if (isPostDeleted) {
                tvRepostImage.gone()
                sensitiveChatSender.gone()
                cvShowPost.gone()
                return
            }
            post?.let { nonNullPost ->
                if (nonNullPost.isAdultContent == true
                    && !img.isNullOrEmpty()
                ) {
                    blurHelper.blurByUrl(img) {
                        tvRepostImage.invisible()
                        ivBluredContent.loadGlide(it)
                        ivBluredContent.setOnClickListener {
                            if (post.deleted == 0) {
                                onMessageClicked.onShowMoreRepost(post.id)
                            }
                        }
                        sensitiveChatSender.visible()
                        cvShowPost.visible()
                        cvShowPost.setOnClickListener {
                            onMessageClicked.onShowPostClicked(nonNullPost.id)
                            sensitiveChatSender.gone()
                            tvRepostImage.visible()
                        }
                    }
                } else {
                    if (img != null && img != "")
                        tvRepostImage.visible()
                    else tvRepostImage.gone()

                    sensitiveChatSender.gone()
                    cvShowPost.gone()
                }
            } ?: kotlin.run {
                tvRepostImage.visible()
                sensitiveChatSender.gone()
                cvShowPost.gone()
            }
        }

        private fun setupMultimediaView(post: Post?) {
            val assets = post?.assets
            ivMultimediaView.isVisible = !assets.isNullOrEmpty() && assets.size > 1 && !post.deleted.toBoolean()
        }
    }

    inner class ReceiverRepostHolder(view: View) : BaseMessageViewHolder(view), ISwipeableHolder {

        private val messageBubbleContainer: LinearLayout = view.findViewById(R.id.ll_chat_container)
        private val messageLayout: ConstraintLayout = view.findViewById(R.id.layout_chat_message_repost_receiver)

        private val ivRepostUserAvatar: ImageView = view.findViewById(R.id.iv_repost_user_avatar_receiver)
        private val tvRepostUserName: TextView = view.findViewById(R.id.tv_repost_user_name_receiver)
        private val tvRepostCreated: TextView = view.findViewById(R.id.tv_repost_created_receiver)
        private val tvRepostImage: ImageView = view.findViewById(R.id.iv_repost_image_receiver)
        private val videoDurationContainer: View = view.findViewById(R.id.repost_image_receiver_video_duration)

        private val tvPostDeleted: TextView = view.findViewById(R.id.iv_repost_post_deleted_receiver)
        private val tvRepostText: TextViewWithImages = view.findViewById(R.id.tv_repost_text_receiver)
        private val tvRepostTitle: TextViewWithImages = view.findViewById(R.id.tv_repost_title_receiver)
        private val tvRepostType: TextView = view.findViewById(R.id.tv_repost_type_receiver)
        private val tvShowMorePost: TextView = view.findViewById(R.id.tv_show_more_text_receiver)
        private val cvRepostContainer: ConstraintLayout = view.findViewById(R.id.cl_repost_container_receiver)

        private val repostParentType: TextView = view.findViewById(R.id.tv_post_repost_type_receiver)

        // Bottom post + repost container
        private val postRepostContainer: LinearLayout = view.findViewById(R.id.post_repost_container_receiver)
        private val tvPostRepostUserName: TextView = view.findViewById(R.id.tv_post_repost_user_name_receiver)

        private val tvChatMessage: TextView = view.findViewById(R.id.tv_chat_msg_repost_receive)
        private val tvChatMessageTime: TextView = view.findViewById(R.id.tv_chat_time_repost_receive)

        private val llChatContainerIns: LinearLayout = view.findViewById(R.id.ll_chat_container_ins)
        private val ivBluredContent: ImageView = view.findViewById(R.id.iv_blured_content)
        private val sensitiveChat: FrameLayout = view.findViewById(R.id.sensitive_chat)
        private val cvShowPost: CardView = view.findViewById(R.id.cv_show_post)
        private val flReplyContainer: FrameLayout = view.findViewById(R.id.fl_reply_container)

        //music
        private val rootMusic: ConstraintLayout = view.findViewById(R.id.cl_root)
        private val ivMusicAlbum: ImageView = view.findViewById(R.id.iv_music_album)
        private val tvMusicArtist: TextView = view.findViewById(R.id.tv_music_artist)
        private val tvMusicName: TextView = view.findViewById(R.id.tv_music_title)

        private val fwdContainer: LinearLayout = view.findViewById(R.id.forward_container_receive)
        private val tvFwdAuthorName: TextView = view.findViewById(R.id.tv_fwd_author_name)
        private val tvMessageEdited: TextView = view.findViewById(R.id.tv_message_edited)
        private val tvUserSenderName: TextView = view.findViewById(R.id.tv_user_sender_name)
        private val ivPencilImage: ImageView = view.findViewById(R.id.imageView32)
        private val ivSenderAvatar: ImageView = view.findViewById(R.id.iv_sender_avatar)

        private val chatEventChips: EventChipsView = view.findViewById(R.id.ecv_event_chips)

        private val ivMultimediaView: View = view.findViewById(R.id.iv_repost_multimedia_view_receiver)

        private var isReplyHasMoved = false

        private var message: MessageEntity? = null

        override fun canSwipe() = isMessageSwipeEnabled(message)

        override fun getSwipeContainer() = flReplyContainer

        fun bind(message: MessageEntity?, prevMessage: MessageEntity?) {
            message?.let { currentMessage ->
                tvMessageEdited.isVisible = isMessageEditEnabled && currentMessage.isEdited()
                this.message = currentMessage
                handleEmojis(tvChatMessage, currentMessage)
                setMessageTime(tvChatMessageTime, currentMessage.createdAt)
                setMessageContainerTopMargin(
                    messageLayout,
                    currentMessage,
                    prevMessage
                )
                handleReplyMessage(currentMessage, onMessageClicked)
                highlightMessageScrollAnimation(false, currentMessage, messageLayout)
                handleMessageHeader(itemView.context, messageLayout, currentMessage) { isVisibleDivider ->
                    // Dirty HACK !
                    if (isVisibleDivider) {
                        llChatContainerIns.setMargins(top = dpToPx(12))
                    }   else {
                        llChatContainerIns.setMargins(top = dpToPx(0))
                    }
                }

                if (roomType == ROOM_TYPE_GROUP && !currentMessage.creator?.name.isNullOrEmpty()) {
                    ivSenderAvatar.visible()
                    ivSenderAvatar.loadGlideCircle(currentMessage.creator?.avatarSmall)
                    tvUserSenderName.visible()
                    tvUserSenderName.text = currentMessage.creator?.name.toString()
                    ivPencilImage.setMargins(top = dpToPx(4))
                } else {
                    ivSenderAvatar.gone()
                    tvUserSenderName.gone()
                    ivPencilImage.setMargins(top = dpToPx(10))
                }

                if (currentMessage.content.isEmpty()) tvChatMessage.gone()
                else tvChatMessage.visible()

                val postMap = message.attachment.metadata[META_DATA_POST] as? LinkedTreeMap<String, Any>
                if (postMap == null) {
                    hideMusicContainer()
                    hideEventContainer()
                }
                postMap?.let {
                    val post = gson.fromJson<Post?>(postMap)
                    tvRepostType.setText(if (post?.event != null) {
                        R.string.chat_repost_event_user_title
                    } else {
                        R.string.chat_repost_user_title
                    })
                    tvRepostUserName.handlePostAuthorStatuses(post)
                    tvRepostUserName.text = post?.user?.name ?: ""
                    ivRepostUserAvatar.loadGlideCircleWithPlaceHolder(post?.user?.avatarSmall, R.drawable.fill_8_round)
                    post?.text?.let {
                        if (post.text.isNullOrEmpty()) tvRepostText.gone()
                        else tvRepostText.visible()

                        trimPostLength(tvRepostText, tvShowMorePost, true, post?.id)
                        handleRepostTextUniqueNames(post, tvRepostText, R.color.ui_purple)
                    }
                    if (post?.event != null && post.event?.title.isNullOrEmpty().not() && isEventsEnabled) {
                        handleRepostTitleUniqueNames(post, tvRepostTitle, R.color.ui_color_chat_send_grey)
                        tvRepostTitle.visible()
                    } else {
                        tvRepostTitle.gone()
                    }

                    val assetPreview = post?.asset?.metadata?.preview
                    var img: String? = null
                    val assets = post?.assets
                    val media = if (!assets.isNullOrEmpty()) {
                        assets[0]
                    } else null

                    if (assetPreview != null) {
                        tvRepostImage.loadGlide(assetPreview)
                        img = assetPreview
                        tvRepostImage.visible()
                    } else {
                        val assetUrl: String? = when {
                            !post?.asset?.url.isNullOrEmpty() -> post?.asset?.url
                            media != null -> getUrlFromMedia(media)
                            else -> null
                        }
                        if (assetUrl != null) {
                            if (post?.postType == PostTypeEnum.AVATAR_VISIBLE.value ||
                                post?.postType == PostTypeEnum.AVATAR_HIDDEN.value) {
                                tvRepostImage.setPadding(dpToPx(24), dpToPx(24), dpToPx(24), dpToPx(24))
                                tvRepostImage.loadGlideCircle(assetUrl)
                            } else {
                                tvRepostImage.setPadding(0, 0, 0, 0)
                                tvRepostImage.loadGlide(assetUrl)
                            }
                            img = assetUrl
                            tvRepostImage.visible()
                        } else {
                            tvRepostImage.gone()
                        }
                    }
                    val aspect = media?.metadata?.aspect ?: post?.asset?.metadata?.aspect
                    setupAspect(tvRepostImage, aspect)

                    val videoDuration = media?.metadata?.duration ?: post?.asset?.metadata?.duration
                    videoDuration?.let { duration ->
                        if (duration != 0) {
                            val durationText = getDurationSeconds(duration)
                            videoDurationContainer.visible()
                            val time = videoDurationContainer.findViewById<TextView>(R.id.exo_position)
                            time.text = durationText
                        } else {
                            videoDurationContainer.gone()
                        }
                    } ?: kotlin.run {
                        videoDurationContainer.gone()
                    }

                    if (post?.parentPost == null) {
                        postRepostContainer.gone()
                    } else {
                        postRepostContainer.visible()
                        post.parentPost?.let { parentPost ->
                            repostParentType.setText(
                                if (parentPost.event != null) R.string.event else R.string.post
                            )
                            tvPostRepostUserName.text = parentPost.user?.name ?: ""
                            postRepostContainer.setOnClickListener {
                                if (post.deleted == 0)
                                    onMessageClicked.onShowMoreRepost(post.id)
                            }
                        }

                    }

                    if (post?.deleted == 1) {
                        tvPostDeleted.visible()
                        tvPostDeleted.text = itemView.resources.getString(R.string.chat_repost_post_deleted)
                        tvRepostImage.gone()
                        videoDurationContainer.gone()
                        tvRepostText.gone()
                    } else {
                        tvPostDeleted.gone()
                        tvRepostText.visible()
                        post?.id?.let { postID ->
                            tvRepostImage.setOnClickListener { onMessageClicked.onShowMoreRepost(postID) }
                        }
                    }

                    post?.id?.let { postID ->
                        tvRepostUserName.setOnClickListener {
                            if (post.deleted == 1) return@setOnClickListener
                            onMessageClicked.onShowMoreRepost(postID)
                        }
                        ivRepostUserAvatar.setOnClickListener {
                            if (post.deleted == 1) return@setOnClickListener
                            onMessageClicked.onShowMoreRepost(postID)
                        }
                        rootMusic.setOnClickListener {
                            if (post.deleted == 1) return@setOnClickListener
                            onMessageClicked.onShowMoreRepost(postID)
                        }
                        chatEventChips.setOnClickListener {
                            if (post.deleted == 1) return@setOnClickListener
                            onMessageClicked.onShowMoreRepost(postID)
                        }
                    }

                    post?.createdAt?.let { dataPost ->
                        tvRepostCreated.text = timeAgo(dataPost / 1000, true)
                    }

                    if (post?.text.isNullOrEmpty()) tvRepostText.gone()
                    else tvRepostText.visible()

                    setupBlur(post, img, post?.deleted.toBoolean())

                    messageBubbleContainer.setOnLongClickListener { true }

                    cvRepostContainer.setOnLongClickListener {
                        if (!isReplyHasMoved) {
                            onMessageClicked.onMessageLongClicked(message, cvRepostContainer)
                        }
                        true
                    }
                    tvChatMessage.setOnLongClickListener {
                        if (!isReplyHasMoved) {
                            onMessageClicked.onMessageLongClicked(message, tvChatMessage)
                        }
                        true
                    }
                    setupMusicContainer(post?.mediaEntity)
                    setupEventContainer(
                        event = post?.event,
                        postDeleted = post?.deleted.toBoolean()
                    )
                    setupMultimediaView(post)

                    handleMessageForwarding(currentMessage, fwdContainer, tvFwdAuthorName, onMessageClicked)
                }

                setupContentForMoment(currentMessage)
            }
        }

        /**
         * Техдолг: https://nomera.atlassian.net/browse/BR-17753
         * Необходимо выпилить Dto из адаптеров
         */
        private fun setupContentForMoment(currentMessage: MessageEntity) {
            val momentMap = currentMessage.attachment.metadata[META_DATA_MOMENT] as? LinkedTreeMap<String, Any> ?: return
            val moment = gson.fromJson<MomentItemDto?>(momentMap) ?: return
            tvRepostUserName.text = moment.user?.name ?: ""
            tvRepostType.text = itemView.resources.getString(R.string.chat_repost_moment_title)
            ivRepostUserAvatar.loadGlideCircleWithPlaceHolder(
                path = moment.user?.avatarSmall,
                placeholderResId = R.drawable.fill_8_round
            )
            moment.setContentVisibilityForMoment()
            moment.asset?.preview?.let { tvRepostImage.loadGlide(it) }
            setupAspect(view = tvRepostImage, aspect = null)
            if (moment.deleted.toBoolean()) {
                tvPostDeleted.text = itemView.resources.getString(R.string.chat_repost_moment_unavailable)
            } else {
                tvRepostImage.setOnClickListener { onMessageClicked.onShowRepostMoment(moment.id) }
                tvRepostUserName.setOnClickListener { onMessageClicked.onShowRepostMoment(moment.id) }
                ivRepostUserAvatar.setOnClickListener { onMessageClicked.onShowRepostMoment(moment.id) }
            }
            tvRepostCreated.text = timeAgo(moment.createdAt, true)
            cvRepostContainer.setOnLongClickListener {
                if (!isReplyHasMoved) onMessageClicked.onMessageLongClicked(
                    message = message,
                    messageView = cvRepostContainer
                )
                true
            }
            tvChatMessage.setOnLongClickListener {
                if (!isReplyHasMoved) onMessageClicked.onMessageLongClicked(
                    message = message,
                    messageView = tvChatMessage
                )
                true
            }
            handleMessageForwarding(
                message = currentMessage,
                container = fwdContainer,
                tvName = tvFwdAuthorName,
                onMessageClicked = onMessageClicked
            )
        }

        private fun MomentItemDto.setContentVisibilityForMoment() {
            tvPostDeleted.isVisible = deleted.toBoolean()
            tvRepostImage.isVisible = !deleted.toBoolean()
            videoDurationContainer.gone()
            tvRepostText.gone()
            tvRepostTitle.gone()
            tvShowMorePost.gone()
            postRepostContainer.gone()
            sensitiveChat.gone()
            cvShowPost.gone()
            hideMusicContainer()
            hideEventContainer()
        }

        private fun setupMusicContainer(media: MediaEntity?) {
            media?.let {
                rootMusic.visible()
                tvMusicArtist.text = media.artist
                tvMusicName.text = media.track
                tvMusicName.setTextColor(Color.BLACK)
                tvMusicArtist.setTextColor(ContextCompat.getColor(itemView.context, R.color.ui_gray_80))
                ivMusicAlbum.loadGlide(media.albumUrl)
            } ?: kotlin.run {
                hideMusicContainer()
            }
        }

        private fun setupEventContainer(event: EventDto?, postDeleted: Boolean) {
            if (event != null && postDeleted.not() && isEventsEnabled) {
                val chipsUiModel = EventChipsUiModel(
                    type = EventChipsType.LIGHT,
                    label = chatMessageEventLabelUiMapper.mapEventLabelUiModel(
                        eventDto = event,
                        isSenderMessage = false
                    )
                )
                chatEventChips.visible()
                chatEventChips.setModel(chipsUiModel)
            } else {
                hideEventContainer()
            }
        }

        private fun hideMusicContainer() {
            rootMusic.gone()
        }

        private fun hideEventContainer() {
            chatEventChips.gone()
        }

        private fun setupAspect(view: View, aspect: Float?) {
            Timber.d("setupAspect aspect = $aspect")
            view.post {
                aspect?.let {
                    if (aspect > 0.toDouble()) {
                        val layoutParams = view.layoutParams
                        layoutParams?.width = view.width
                        layoutParams?.height = (view.width / aspect).toInt()
                        view.layoutParams = layoutParams
                    }
                } ?: kotlin.run {
                    val layoutParams = view.layoutParams
                    layoutParams?.width = MATCH_PARENT
                    layoutParams?.height = WRAP_CONTENT
                    view.layoutParams = layoutParams
                }
            }
        }

        private fun setupBlur(post: Post?, img: String?, isPostDeleted: Boolean) {
            if (isPostDeleted) {
                tvRepostImage.gone()
                sensitiveChat.gone()
                cvShowPost.gone()
                return
            }
            post?.let { nonNullPost ->
                if (nonNullPost.isAdultContent == true && !img.isNullOrEmpty()) {
                    blurHelper.blurByUrl(img) {
                        tvRepostImage.invisible()
                        ivBluredContent.loadGlide(it)
                        ivBluredContent.setOnClickListener {
                            if (post.deleted == 0) {
                                onMessageClicked.onShowMoreRepost(post.id)
                            }
                        }
                        sensitiveChat.visible()
                        cvShowPost.visible()
                        cvShowPost.setOnClickListener {
                            onMessageClicked.onShowPostClicked(nonNullPost.id)
                            sensitiveChat.gone()
                            tvRepostImage.visible()
                        }
                    }
                } else {
                    if (img != null && img != "")
                        tvRepostImage.visible()
                    else tvRepostImage.gone()

                    sensitiveChat.gone()
                    cvShowPost.gone()
                }
            } ?: kotlin.run {
                tvRepostImage.visible()
                sensitiveChat.gone()
                cvShowPost.gone()
            }
        }

        private fun setupMultimediaView(post: Post?) {
            val assets = post?.assets
            ivMultimediaView.isVisible = !assets.isNullOrEmpty() && assets.size > 1 && !post.deleted.toBoolean()
        }
    }

    /**
     * View holder for SENDER audio message
     */
    inner class SenderVoiceMessageHolder(
        view: View,
        private val onMessageClicked: IOnMessageClickedNew
    ) : BaseMessageViewHolder(view), ISwipeableHolder {

        private val llMessageBubbleContainer: FrameLayout =
            view.findViewById(R.id.ll_message_bubble_container_audio_sender)
        private val llMessageBubble: LinearLayout = view.findViewById(R.id.ll_chat_bubble_background)

        private val messageLayout: LinearLayout = view.findViewById(R.id.layout_chat_message)

        private val voiceMessageView = view.findViewById<VoiceMessageView>(R.id.voice_message_view)
        private val tvChatTime: TextView = view.findViewById(R.id.tv_chat_time)

        // Text recognition
        private val ivExpandCollapseText: ImageView = view.findViewById(R.id.iv_expand_collapse_text)
        private val expandableContainer: ExpandableLayout = view.findViewById(R.id.expand_container)
        private val tvRecognizedText: TextView = view.findViewById(R.id.tv_recognized_text_sender)

        // Resend message
        private val ivResendError: ImageView = view.findViewById(R.id.iv_sender_audio_message)
        private val resendTapContainer: FrameLayout = view.findViewById(R.id.resend_tap_container)

        // Delivered / Read
        private val ivMarkerSent: ImageView = view.findViewById(R.id.iv_marker_sent)
        private val ivMarkerDelivered: ImageView = view.findViewById(R.id.iv_marker_delivered)
        private val ivMarkerRead: ImageView = view.findViewById(R.id.iv_marker_read)

        private val fwdContainer: LinearLayout = view.findViewById(R.id.forward_container_send)
        private val tvFwdAuthorName: TextView = view.findViewById(R.id.tv_fwd_author_name)

        private var message: MessageEntity? = null

        override fun canSwipe() = isMessageSwipeEnabled(message)

        override fun getSwipeContainer() = llMessageBubbleContainer

        private val voiceTextActionHelper = RecognizedVoiceTextActionHelper()

        fun bind(
            message: MessageEntity?,
            prevMessage: MessageEntity?
        ) {
            message?.let { currentMessage ->
                this.message = currentMessage
                setupVoiceMessage(currentMessage, voiceMessageView)
                itemView.tag = message.msgId
                setMessageTime(tvChatTime, message.createdAt)
                handleActionProgress(
                    ivResendError,
                    ivMarkerSent,
                    llMessageBubbleContainer,
                    resendTapContainer,
                    message
                )
                handleMessageStatus(
                    ivMarkerDelivered,
                    ivMarkerRead,
                    ivMarkerSent,
                    message.delivered,
                    message.readed,
                    message.sent
                )
                setMessageContainerTopMargin(
                    messageLayout,
                    currentMessage,
                    prevMessage
                )
                highlightMessageScrollAnimation(
                    true,
                    currentMessage,
                    llMessageBubbleContainer,
                )
                handleReplyMessage(currentMessage, onMessageClicked)
                onMessageClicked.onVoiceMessagebinded(voiceMessageView, message, false)

                fwdContainer.layoutParams = LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
                fwdContainer.setMargins(
                    start = FWD_CONTAINER_VOICE_MSG_MARGIN.start,
                    top = FWD_CONTAINER_VOICE_MSG_MARGIN.top,
                    end = FWD_CONTAINER_VOICE_MSG_MARGIN.end
                )
                val isFwdEnabled = handleMessageForwarding(
                    currentMessage, fwdContainer, tvFwdAuthorName, onMessageClicked
                )

                llMessageBubble.setOnLongClickListener {
                    onMessageClicked.onVoiceMessageLongClicked(message)
                    true
                }

                expandableContainer.longClick { view ->
                    onMessageClicked.onVoiceMessageLongClicked(
                        message = message,
                        recognizedText = message.attachment.audioRecognizedText,
                        messageView = view
                    )
                }

                voiceTextActionHelper.handleAudioRecognizedTextExpandLayout(
                    isSend = true,
                    message = currentMessage,
                    textView = tvRecognizedText,
                    btnExpand = ivExpandCollapseText,
                    container = expandableContainer,
                    isForwardMessage = isFwdEnabled,
                    isExpandAction = { isExpanded ->
                        onMessageClicked.onExpandVoiceMessageText(currentMessage, isExpanded)
                    },
                    onBtnAnimationComplete = {
                        onMessageClicked.onExpandBtnAnimationCompleteVoiceMessage(currentMessage)
                    }
                )

            }
        }

        fun bindRecognizedText(message: MessageEntity?) {
            message?.let { currentMessage ->
                val isFwdEnabled = handleMessageForwarding(
                    currentMessage, fwdContainer, tvFwdAuthorName, onMessageClicked
                )

                voiceTextActionHelper.handleAudioRecognizedTextExpandLayout(
                    isSend = true,
                    message = currentMessage,
                    textView = tvRecognizedText,
                    btnExpand = ivExpandCollapseText,
                    container = expandableContainer,
                    isForwardMessage = isFwdEnabled,
                    isExpandAction = { isExpanded ->
                        onMessageClicked.onExpandVoiceMessageText(currentMessage, isExpanded)
                    },
                    onBtnAnimationComplete = {
                        onMessageClicked.onExpandBtnAnimationCompleteVoiceMessage(currentMessage)
                    }
                )
            }

            bindPlayVoice(message)
        }

        fun bindPlayVoice(message: MessageEntity?) {
            refreshVoiceMessage(message, voiceMessageView, isIncomingMessage = false)
        }

    }


    /**
     * View holder for RECEIVER audio message
     */
    inner class ReceiverVoiceMessageHolder(
        view: View,
        private val onMessageClicked: IOnMessageClickedNew
    ) : BaseMessageViewHolder(view), ISwipeableHolder {

        private val messageBubbleContainer: LinearLayout = view.findViewById(R.id.ll_chat_container)
        private val llMessageBubble: LinearLayout = view.findViewById(R.id.ll_chat_bubble_background)
        private val messageLayout: LinearLayout = view.findViewById(R.id.layout_chat_message)
        private val ivUserAvatar: ImageView = view.findViewById(R.id.iv_user_avatar)
        private val voiceMessageView = view.findViewById<VoiceMessageView>(R.id.voice_message_view)
        private val tvChatTime: TextView = view.findViewById(R.id.tv_chat_time)

        // Text recognition
        private val ivExpandCollapseText: ImageView = view.findViewById(R.id.iv_expand_collapse_text)
        private val expandableContainer: ExpandableLayout = view.findViewById(R.id.expand_container)
        private val tvRecognizedText: TextView = view.findViewById(R.id.tv_recognized_text_receiver)

        private val fwdContainer: LinearLayout = view.findViewById(R.id.forward_container_receive)
        private val tvFwdAuthorName: TextView = view.findViewById(R.id.tv_fwd_author_name)

        private var message: MessageEntity? = null

        override fun canSwipe() = isMessageSwipeEnabled(message)

        override fun getSwipeContainer() = messageBubbleContainer

        private val voiceTextActionHelper = RecognizedVoiceTextActionHelper()

        fun bind(
            message: MessageEntity?,
            roomType: String?,
            prevMessage: MessageEntity?
        ) {
            message?.let { currentMessage ->
                // Avatar
                this.message = currentMessage
                if (roomType == ROOM_TYPE_GROUP) {
                    ivUserAvatar.visible()
                    ivUserAvatar.loadGlideCircle(message.creator?.avatarSmall)
                }

                setupVoiceMessage(currentMessage, voiceMessageView)

                itemView.tag = message.msgId
                setMessageTime(tvChatTime, message.createdAt)
                setMessageContainerTopMargin(
                    messageLayout,
                    currentMessage,
                    prevMessage
                )
                handleMessageHeader(itemView.context, messageLayout, currentMessage)
                handleReplyMessage(currentMessage, onMessageClicked)
                highlightMessageScrollAnimation(false, currentMessage, messageBubbleContainer)

                fwdContainer.layoutParams = LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
                fwdContainer.setMargins(
                    start = FWD_CONTAINER_VOICE_MSG_MARGIN.start,
                    top = FWD_CONTAINER_VOICE_MSG_MARGIN.top,
                    end = FWD_CONTAINER_VOICE_MSG_MARGIN.end
                )
                val isFwdEnabled = handleMessageForwarding(
                    currentMessage, fwdContainer, tvFwdAuthorName, onMessageClicked
                )

                llMessageBubble.setOnLongClickListener {
                    onMessageClicked.onVoiceMessageLongClicked(message)
                    true
                }

                expandableContainer.longClick { view ->
                    onMessageClicked.onVoiceMessageLongClicked(
                        message = message,
                        recognizedText = message.attachment.audioRecognizedText,
                        messageView = view
                    )
                }

                voiceTextActionHelper.handleAudioRecognizedTextExpandLayout(
                    isSend = false,
                    message = currentMessage,
                    textView = tvRecognizedText,
                    btnExpand = ivExpandCollapseText,
                    container = expandableContainer,
                    isForwardMessage = isFwdEnabled,
                    isExpandAction = { isExpanded ->
                        onMessageClicked.onExpandVoiceMessageText(currentMessage, isExpanded)
                    },
                    onBtnAnimationComplete = {
                        onMessageClicked.onExpandBtnAnimationCompleteVoiceMessage(currentMessage)
                    }
                )

                onMessageClicked.onVoiceMessagebinded(voiceMessageView, message, true)
            }
        }

        fun bindRecognizedText(message: MessageEntity?) {
            message?.let { currentMessage ->
                val isFwdEnabled = handleMessageForwarding(
                    currentMessage, fwdContainer, tvFwdAuthorName, onMessageClicked
                )

                voiceTextActionHelper.handleAudioRecognizedTextExpandLayout(
                    isSend = false,
                    message = currentMessage,
                    textView = tvRecognizedText,
                    btnExpand = ivExpandCollapseText,
                    container = expandableContainer,
                    isForwardMessage = isFwdEnabled,
                    isExpandAction = { isExpanded ->
                        onMessageClicked.onExpandVoiceMessageText(currentMessage, isExpanded)
                    },
                    onBtnAnimationComplete = {
                        onMessageClicked.onExpandBtnAnimationCompleteVoiceMessage(currentMessage)
                    }
                )
            }

            bindPlayVoice(message)
        }

        fun bindPlayVoice(message: MessageEntity?) {
            refreshVoiceMessage(message, voiceMessageView, isIncomingMessage = true)
        }

    }

    inner class ReceiveTextMessage(
        view: View,
        private val onMessageClicked: IOnMessageClickedNew
    ) : BaseMessageViewHolder(view), View.OnClickListener, View.OnLongClickListener, ISwipeableHolder {

        private val wholeMessageContainer: FrameLayout = view.findViewById(R.id.whole_message_width_container)
        private val llMessageBubbleContainer: LinearLayout = view.findViewById(R.id.receive_message_container)

        private val tvChatMessage: TextView = view.findViewById(R.id.tv_chat_msg)
        private val tvChatTime: TextView = view.findViewById(R.id.tv_chat_time)
        private val messageLayout: ViewGroup = view.findViewById(R.id.layout_chat_message)

        private val tvUserName: TextView = view.findViewById(R.id.tv_user_name)
        private val ivUserAvatar: ImageView = view.findViewById(R.id.iv_user_avatar)
        private val bubbleContainer: LinearLayout = view.findViewById(R.id.receive_text_bubble_container)

        private var messageEntity: MessageEntity? = null

        private val fwdContainer: LinearLayout = view.findViewById(R.id.forward_container_receive)
        private val tvFwdAuthorName: TextView = view.findViewById(R.id.tv_fwd_author_name)
        private val tvMessageEdited: TextView = view.findViewById(R.id.tv_message_edited)

        override fun canSwipe() = isMessageSwipeEnabled(messageEntity)

        override fun getSwipeContainer() = llMessageBubbleContainer

        fun bind(
            message: MessageEntity?,
            prevMessage: MessageEntity?,
            nextMessage: MessageEntity?
        ) {
            message?.let { currentMessage ->
                tvMessageEdited.isVisible = isMessageEditEnabled && currentMessage.isEdited()
                this.messageEntity = currentMessage
                itemView.tag = message.msgId

                handleEmojis(tvChatMessage, currentMessage)
                setMessageTime(tvChatTime, message.createdAt)
                setMessageContainerTopMargin(
                    messageLayout,
                    currentMessage,
                    prevMessage
                )
                handleMessageHeader(itemView.context, messageLayout, currentMessage)
                handleReplyMessage(currentMessage, onMessageClicked)
                highlightMessageScrollAnimation(false, currentMessage, wholeMessageContainer)

                // User Avatar in a group chat
                if (roomType == ROOM_TYPE_GROUP) {
                    tvUserName.text = message.creator?.name
                    tvUserName.visible()
                    bubbleContainer.setMargins(dpToPx(10))

                    // Hide user avatar in previous message if message author is the same in a group chat
                    if (message.creator?.userId != nextMessage?.creator?.userId) {
                        ivUserAvatar.visible()
                        if (message.creator?.avatarSmall.isNullOrEmpty()) {
                            ivUserAvatar.loadGlideCircle(R.drawable.fill_8_round)
                        } else ivUserAvatar.loadGlideCircle(message.creator?.avatarSmall)
                    } else {
                        ivUserAvatar.invisible()
                    }
                }

                handleMessageForwarding(currentMessage, fwdContainer, tvFwdAuthorName, onMessageClicked)

                messageLayout.setOnClickListener(this)
                messageLayout.setOnLongClickListener(this)
                tvChatMessage.setOnLongClickListener(this)
                llMessageBubbleContainer.setOnLongClickListener(this)
            }
        }

        override fun onClick(v: View?) {
            /** STUB */
        }

        override fun onLongClick(v: View?): Boolean {
            onMessageClicked.onMessageLongClicked(messageEntity, v)
            return true
        }
    }

    inner class SenderTextMessage(
        view: View,
        private val onMessageClicked: IOnMessageClickedNew
    ) : BaseMessageViewHolder(view), View.OnClickListener, View.OnLongClickListener, ISwipeableHolder {

        private val llMessageBubbleContainer: LinearLayout = view.findViewById(R.id.ll_message_bubble_container)
        private val messageContainer: FrameLayout = view.findViewById(R.id.only_message_bubble_container)
        private val messageLayout: ViewGroup = view.findViewById(R.id.layout_chat_message)
        private val tvChatMessage: TextView = view.findViewById(R.id.tv_chat_msg)
        private val tvChatTime: TextView = view.findViewById(R.id.tv_chat_time)
        private val ivMarkerSent: ImageView = view.findViewById(R.id.iv_marker_sent)
        private val ivMarkerDelivered: ImageView = view.findViewById(R.id.iv_marker_delivered)
        private val ivMarkerRead: ImageView = view.findViewById(R.id.iv_marker_read)

        // Resend message
        private val ivResendError: ImageView = view.findViewById(R.id.iv_sender_simple_message_error)
        private val resendTapContainer: FrameLayout = view.findViewById(R.id.resend_tap_container)

        private val fwdContainer: LinearLayout = view.findViewById(R.id.forward_container_send)
        private val tvFwdAuthorName: TextView = view.findViewById(R.id.tv_fwd_author_name)
        private val tvMessageEdited: TextView = view.findViewById(R.id.tv_message_edited)

        private var messageEntity: MessageEntity? = null

        private var message: MessageEntity? = null

        override fun canSwipe() = isMessageSwipeEnabled(message)

        override fun getSwipeContainer() = llMessageBubbleContainer

        fun bind(message: MessageEntity?, prevMessage: MessageEntity?) {

            message?.let { currentMessage ->
                tvMessageEdited.isVisible = isMessageEditEnabled && currentMessage.isEdited()
                this.message = currentMessage
                messageLayout.visible()
                this.messageEntity = currentMessage
                itemView.tag = message.msgId

                handleEmojis(tvChatMessage, currentMessage)
                setMessageTime(tvChatTime, message.createdAt)
                setMessageContainerTopMargin(
                    messageLayout,
                    currentMessage,
                    prevMessage
                )
                handleActionProgress(
                    ivResendError,
                    ivMarkerSent,
                    llMessageBubbleContainer,
                    resendTapContainer,
                    message,
                    tvChatMessage
                )
                handleMessageStatus(
                    ivMarkerDelivered,
                    ivMarkerRead,
                    ivMarkerSent,
                    message.delivered,
                    message.readed,
                    message.sent
                )
                highlightMessageScrollAnimation(true, currentMessage, messageContainer)
                handleReplyMessage(currentMessage, onMessageClicked)

                messageLayout.setOnClickListener(this)
                messageLayout.setOnLongClickListener(this)
                tvChatMessage.setOnLongClickListener(this)
                llMessageBubbleContainer.setOnLongClickListener(this)

                handleMessageForwarding(currentMessage, fwdContainer, tvFwdAuthorName, onMessageClicked)
                if (fwdContainer.isVisible) tvChatMessage.setMargins(
                    top = FWD_CONTAINER_MARGIN_BOTTOM_4.dp
                )

            } ?: kotlin.run {
                messageLayout.gone()
            }
        }

        override fun onClick(v: View?) {
            /** STUB */
        }

        override fun onLongClick(v: View?): Boolean {
            onMessageClicked.onMessageLongClicked(messageEntity, v)
            return true
        }
    }


    /**
     * Message holder for SENDER (your messages)
     */
    inner class SenderMessageHolder(
        view: View,
        private val onMessageClicked: IOnMessageClickedNew
    ) : BaseMessageViewHolder(view), View.OnClickListener, View.OnLongClickListener, ISwipeableHolder {

        private val llMessageBubbleContainer: ConstraintLayout =
            view.findViewById(R.id.vg_message_bubble_container_message_send)

        private val messageLayout: ViewGroup = view.findViewById(R.id.vg_layout_chat_message)
        private val tvChatMessage: TextView = view.findViewById(R.id.tv_chat_msg)
        private val tvChatTime: TextView = view.findViewById(R.id.tv_chat_time)

        private val ivMarkerSent: ImageView = view.findViewById(R.id.iv_marker_sent)
        private val ivMarkerDelivered: ImageView = view.findViewById(R.id.iv_marker_delivered)
        private val ivMarkerRead: ImageView = view.findViewById(R.id.iv_marker_read)

        // Resend message
        private val ivResendError: ImageView = view.findViewById(R.id.iv_send_message_error)
        private val resendTapContainer: FrameLayout = view.findViewById(R.id.vg_resend_tap_container)
        private val pbProgress: ProgressBar = view.findViewById(R.id.pb_progress)

        private val fwdContainer: LinearLayout = view.findViewById(R.id.forward_container_send)
        private val tvFwdAuthorName: TextView = view.findViewById(R.id.tv_fwd_author_name)
        private val tvMessageEdited: TextView = view.findViewById(R.id.tv_message_edited)

        private var messageEntity: MessageEntity? = null


        private val gridLayoutManager = NoScrollableGridLayoutManager(itemView.context, MAX_ROWS).apply {
            initialPrefetchItemCount = 5
        }

        private val rvChatImages = view.findViewById<RecyclerView>(R.id.rv_sender_recycler).apply {
            layoutManager = gridLayoutManager
            isNestedScrollingEnabled = false
            setHasFixedSize(true)
        }

        override fun canSwipe() = isMessageSwipeEnabled(messageEntity)

        override fun getSwipeContainer() = llMessageBubbleContainer

        @SuppressLint("SetTextI18n")
        fun bind(message: MessageEntity?, prevMessage: MessageEntity?) {
            message?.let { currentMessage ->
                tvMessageEdited.isVisible = isMessageEditEnabled && currentMessage.isEdited()
                this.messageEntity = currentMessage
                itemView.tag = message.msgId

                handleMessageImageAttachments(message)
                setMessageTime(tvChatTime, message.createdAt)
                setMessageContainerTopMargin(
                    messageLayout,
                    currentMessage,
                    prevMessage
                )
                handleActionProgress(
                    ivResendError,
                    ivMarkerSent,
                    llMessageBubbleContainer,
                    resendTapContainer,
                    message,
                    tvChatMessage,
                    pbProgress = pbProgress
                )
                handleMessageStatus(
                    ivMarkerDelivered,
                    ivMarkerRead,
                    ivMarkerSent,
                    message.delivered,
                    message.readed,
                    message.sent
                )
                highlightMessageScrollAnimation(true, currentMessage, llMessageBubbleContainer)
                handleReplyMessage(currentMessage, onMessageClicked)
                llMessageBubbleContainer.setOnClickListener(this)
                llMessageBubbleContainer.setOnLongClickListener(this)

                fwdContainer.setMargins(bottom = FWD_CONTAINER_MARGIN_BOTTOM_10.dp)
                handleMessageForwarding(currentMessage, fwdContainer, tvFwdAuthorName, onMessageClicked)
            }
        }

        private fun handleMessageImageAttachments(message: MessageEntity) {
            val attachments = message.attachments
            if (attachments.isNotEmpty()) { //here we can take a several images
                val uris = mutableListOf<String>()
                attachments.forEach {
                    uris.add(it.url)
                }
                if (uris.isNotEmpty()) {
                    rvChatImages.visible()
                    tvChatMessage.measure(0, 0)
                    setNestedImagesRecycler(
                        message = message,
                        recyclerView = rvChatImages,
                        blurContainer = null,
                        gridLayoutManager = gridLayoutManager,
                        images = uris,
                        longClickListener = this,
                        aspectRatio = if (uris.size == 1) {
                            message.attachment.ratio
                        } else null,
                        minimalWidth = tvChatMessage.measuredWidth,
                        isMyImages = true,
                    )
                    handleEmojis(tvChatMessage, message, false)
                    if (tvChatMessage.text.isNullOrEmpty()) tvChatMessage.gone()
                    else tvChatMessage.visible()
                } else {
                    handleEmojis(tvChatMessage, message, true)
                    handleAttachment(message)
                }
            } else {
                handleEmojis(tvChatMessage, message, false)
                handleAttachment(message)
            }
        }

        private fun handleAttachment(message: MessageEntity) {
            val attachment = message.attachment
            if ((attachment.type == TYPING_TYPE_IMAGE || attachment.type == TYPING_TYPE_GIF)
                && attachment.url.isNotEmpty()
            ) {
                rvChatImages.visible()
                tvChatMessage.measure(0, 0)
                setNestedImagesRecycler(
                    message = message,
                    recyclerView = rvChatImages,
                    blurContainer = null,
                    gridLayoutManager = gridLayoutManager,
                    images = mutableListOf(attachment.url),
                    longClickListener = this,
                    aspectRatio = attachment.ratio,
                    minimalWidth = tvChatMessage.measuredWidth,
                    isMyImages = true,
                )
            } else {
                rvChatImages.gone()
            }
        }

        override fun onClick(view: View?) {
            /** STUB */
        }

        override fun onLongClick(view: View?): Boolean {
            onMessageClicked.onMessageLongClicked(messageEntity, view)
            return true
        }
    }


    /**
     * Sender deleted message
     */
    inner class SendMessageDeletedHolder(
        view: View,
        private val onMessageClicked: IOnMessageClickedNew
    ) : BaseMessageViewHolder(view) {

        private val messageLayout: LinearLayout = view.findViewById(R.id.layout_chat_message)
        private val tvTitle: TextView = view.findViewById(R.id.tv_delete_message_title_sender)
        private val createdAt: TextView = view.findViewById(R.id.tv_sender_created_at)

        fun bind(message: MessageEntity?, prevMessage: MessageEntity?) {
            message?.let { currentMessage ->
                // Title call message deleted
                if (currentMessage.eventCode == ChatEventEnum.CALL.state) {
                    tvTitle.text = itemView.context.getString(R.string.chat_message_you_delete_call)
                } else {
                    tvTitle.text = itemView.context.getString(R.string.chat_message_you_delete)
                }

                setMessageTime(createdAt, currentMessage.createdAt)
                setMessageContainerTopMargin(
                    messageLayout,
                    currentMessage,
                    prevMessage
                )
            }

            itemView.longClick { onMessageClicked.onDeletedMessageLongClicked(message) }
        }
    }


    /**
     * Sender IMAGE Item
     */
    inner class SenderImageHolder(
        view: View,
        private val onMessageClicked: IOnMessageClickedNew
    ) : BaseMessageViewHolder(view), View.OnClickListener, View.OnLongClickListener, ISwipeableHolder {

        private val llMessageBubbleContainer: ConstraintLayout =
            view.findViewById(R.id.vg_message_bubble_container_image_sender)

        private val messageLayout: ViewGroup = view.findViewById(R.id.vg_layout_chat_message)
        private val tvChatTime: TextView = view.findViewById(R.id.tv_chat_time)
        private val ivMarkerSent: ImageView = view.findViewById(R.id.iv_marker_sent)
        private val ivMarkerDelivered: ImageView = view.findViewById(R.id.iv_marker_delivered)
        private val ivMarkerRead: ImageView = view.findViewById(R.id.iv_marker_read)

        // Resend image
        private val ivResendError: ImageView = view.findViewById(R.id.iv_sender_simple_image_message_error)
        private val resendTapContainer: FrameLayout = view.findViewById(R.id.vg_resend_tap_container)
        private val pbProgress: ProgressBar = view.findViewById(R.id.pb_progress)

        private val fwdContainer: LinearLayout = view.findViewById(R.id.forward_container_send)
        private val tvFwdAuthorName: TextView = view.findViewById(R.id.tv_fwd_author_name)
        private val tvMessageEdited: TextView = view.findViewById(R.id.tv_message_edited)

        private val gridLayoutManager = NoScrollableGridLayoutManager(itemView.context, MAX_ROWS).apply {
            initialPrefetchItemCount = 5
        }

        private val rvChatImages = view.findViewById<RecyclerView>(R.id.rv_chat_images).apply {
            layoutManager = gridLayoutManager
            isNestedScrollingEnabled = false
            setHasFixedSize(true)
        }

        private var messageEntity: MessageEntity? = null

        override fun canSwipe() = isMessageSwipeEnabled(messageEntity)

        override fun getSwipeContainer() = llMessageBubbleContainer

        fun bind(message: MessageEntity?, prevMessage: MessageEntity?) {
            message?.let { currentMessage ->
                tvMessageEdited.isVisible = isMessageEditEnabled && currentMessage.isEdited()
                itemView.tag = message.msgId
                this.messageEntity = currentMessage

                setMessageTime(tvChatTime, message.createdAt)

                val img = mutableListOf<String>()
                if (message.attachments.isNotEmpty()) {
                    message.attachments.forEach { image ->
                        // После переотправки сообщения отображаем только существующие локально файлы
                        if (image.url != MessageAttachment.EMPTY_URL) {
                            img.add(image.url)
                        }
                    }
                } else {
                    img.add(message.attachment.url)

                    isFileByLocalPathExists(message.attachment.url) {
                        onMessageClicked.onSetNoMediaPlaceholderMessage(currentMessage)
                    }
                }

                handleActionProgress(
                    ivResendError,
                    ivMarkerSent,
                    llMessageBubbleContainer,
                    resendTapContainer,
                    message,
                    pbProgress = pbProgress
                )
                initImage(img, message)
                handleMessageStatus(
                    ivMarkerDelivered,
                    ivMarkerRead,
                    ivMarkerSent,
                    message.delivered,
                    message.readed,
                    message.sent
                )
                setMessageContainerTopMargin(
                    messageLayout,
                    currentMessage,
                    prevMessage
                )
                highlightMessageScrollAnimation(true, currentMessage, llMessageBubbleContainer)
                handleReplyMessage(currentMessage, onMessageClicked)

                fwdContainer.setMargins(bottom = FWD_CONTAINER_MARGIN_BOTTOM_8.dp)
                handleMessageForwarding(currentMessage, fwdContainer, tvFwdAuthorName, onMessageClicked)

                llMessageBubbleContainer.setOnLongClickListener(this)
            }
        }

        private fun initImage(img: List<String>, message: MessageEntity?) {
            message?.let {
                rvChatImages.visible()
                setNestedImagesRecycler(
                    message = message,
                    recyclerView = rvChatImages,
                    blurContainer = null,
                    gridLayoutManager = gridLayoutManager,
                    images = img,
                    longClickListener = this,
                    aspectRatio = if (img.size == 1) message.attachment.ratio else null,
                    isShowGiphyWatermark = message.isShowGiphyWatermark ?: false,
                    isMyImages = true,
                )
            }
        }

        override fun onClick(p0: View?) {
            onMessageClicked.onAttachmentClicked(messageEntity)
        }

        override fun onLongClick(view: View?): Boolean {
            onMessageClicked.onMessageLongClicked(messageEntity, view)
            return true
        }
    }

    inner class SenderStickerHolder(
        private val binding: ItemChatStickerSendBinding,
        private val onMessageClicked: IOnMessageClickedNew
    ) : BaseMessageViewHolder(binding.root), ISwipeableHolder {

        private var messageEntity: MessageEntity? = null

        override fun canSwipe(): Boolean = isMessageSwipeEnabled(messageEntity)

        override fun getSwipeContainer(): View = binding.root

        fun bind(message: MessageEntity?, prevMessage: MessageEntity?) {
            message?.let { currentMessage ->
                itemView.tag = message.msgId
                val previouslyBoundMessage = this.messageEntity
                this.messageEntity = currentMessage

                setMessageTime(
                    tvTime = binding.tvChatTime,
                    createdAt = message.createdAt
                )

                initSticker(message.attachment, previouslyBoundMessage?.attachment)

                handleActionProgress(
                    ivError = binding.ivSenderSimpleImageMessageError,
                    ivSent = binding.ivMarkerSent,
                    bubbleContainer = binding.vgChatBubbleBackground,
                    tapContainer = binding.vgResendTapContainer,
                    message = message
                )
                handleMessageStatus(
                    ivDelivered = binding.ivMarkerDelivered,
                    ivRead = binding.ivMarkerRead,
                    ivSent = binding.ivMarkerSent,
                    isDelivered = message.delivered,
                    isRead = message.readed,
                    isSent = message.sent
                )
                setMessageContainerTopMargin(
                    messageLayout = binding.vgLayoutChatMessage,
                    currMessage = currentMessage,
                    prevMessage = prevMessage
                )
                highlightMessageScrollAnimation(
                    isSender = true,
                    message = currentMessage,
                    container = binding.root,
                )
                handleReplyMessage(
                    currentMessage = currentMessage,
                    onMessageClicked = onMessageClicked
                )

                handleMessageForwarding(
                    message = currentMessage,
                    container = binding.forwardContainerSend.vgContainerSend,
                    tvName = binding.forwardContainerSend.tvFwdAuthorName,
                    onMessageClicked = onMessageClicked
                )

                binding.vgChatBubbleBackground.setOnLongClickListener {
                    onMessageClicked.onMessageLongClicked(message)
                    true
                }
            }
        }

        private fun initSticker(attachment: MessageAttachment, prevAttachment: MessageAttachment?) {
            val sameAttachment = prevAttachment == attachment
            val isStickerAlreadyShown = (binding.lavChatSticker.isAnimating ||
                binding.ivChatSticker.drawable != null) && sameAttachment
            if (isStickerAlreadyShown) return
            binding.vgStickerPlaceholder.visible()
            when {
                !attachment.lottieUrl.isNullOrBlank() -> {
                    binding.ivChatSticker.gone()
                    binding.lavChatSticker.visible()
                    binding.lavChatSticker.repeatCount = LottieDrawable.INFINITE
                    binding.lavChatSticker.setFailureListener { binding.vgStickerPlaceholder.visible() }
                    binding.lavChatSticker.addLottieOnCompositionLoadedListener { binding.vgStickerPlaceholder.gone() }
                    binding.lavChatSticker.setAnimationFromUrl(attachment.lottieUrl)
                    binding.lavChatSticker.resumeAnimation()
                }
                else -> {
                    binding.ivChatSticker.visible()
                    binding.lavChatSticker.gone()
                    binding.ivChatSticker.loadGlideGifWithCallback(attachment.url, onReady = {
                        binding.vgStickerPlaceholder.gone()
                    })
                }
            }
        }

    }

    inner class ReceiverStickerHolder(
        private val binding: ItemChatStickerReceiveBinding,
        private val onMessageClicked: IOnMessageClickedNew
    ) : BaseMessageViewHolder(binding.root), ISwipeableHolder {

        private var messageEntity: MessageEntity? = null

        override fun canSwipe(): Boolean = isMessageSwipeEnabled(messageEntity)

        override fun getSwipeContainer(): View = binding.root

        fun bind(message: MessageEntity?, prevMessage: MessageEntity?) {
            message?.let { currentMessage ->
                itemView.tag = message.msgId
                val previouslyBoundMessage = messageEntity
                this.messageEntity = currentMessage

                setMessageTime(
                    tvTime = binding.tvChatTime,
                    createdAt = message.createdAt
                )

                initSticker(message.attachment, previouslyBoundMessage?.attachment)

                setMessageContainerTopMargin(
                    messageLayout = binding.vgLayoutChatMessage,
                    currMessage = currentMessage,
                    prevMessage = prevMessage
                )
                highlightMessageScrollAnimation(
                    isSender = false,
                    message = currentMessage,
                    container = binding.root,
                )
                handleReplyMessage(
                    currentMessage = currentMessage,
                    onMessageClicked = onMessageClicked
                )

                handleMessageForwarding(
                    message = currentMessage,
                    container = binding.forwardContainerSend.vgContainerSend,
                    tvName = binding.forwardContainerSend.tvFwdAuthorName,
                    onMessageClicked = onMessageClicked
                )

                binding.vgChatBubbleBackground.setOnLongClickListener {
                    onMessageClicked.onMessageLongClicked(message)
                    true
                }
            }
        }

        private fun initSticker(attachment: MessageAttachment, prevAttachment: MessageAttachment?) {
            val sameAttachment = prevAttachment == attachment
            val isStickerAlreadyShown = (binding.lavChatSticker.isAnimating ||
                binding.ivChatSticker.drawable != null) && sameAttachment
            if (isStickerAlreadyShown) return
            binding.vgStickerPlaceholder.visible()
            when {
                !attachment.lottieUrl.isNullOrBlank() -> {
                    binding.ivChatSticker.gone()
                    binding.lavChatSticker.visible()
                    binding.lavChatSticker.repeatCount = LottieDrawable.INFINITE
                    binding.lavChatSticker.addLottieOnCompositionLoadedListener { binding.vgStickerPlaceholder.gone() }
                    binding.lavChatSticker.setFailureListener { binding.vgStickerPlaceholder.visible() }
                    binding.lavChatSticker.setAnimationFromUrl(attachment.lottieUrl)
                    binding.lavChatSticker.resumeAnimation()
                }
                else -> {
                    binding.ivChatSticker.visible()
                    binding.lavChatSticker.gone()
                    binding.ivChatSticker.loadGlideGifWithCallback(attachment.url, onReady = {
                        binding.vgStickerPlaceholder.gone()
                    })
                }
            }
        }

    }

    /**
     * Message holder for RECEIVER (incoming messages)
     */
    inner class ReceiverMessageHolder(
        view: View,
        private val onMessageClicked: IOnMessageClickedNew
    ) : BaseMessageViewHolder(view), View.OnClickListener, View.OnLongClickListener, ISwipeableHolder {

        private val messageBubbleContainer: ViewGroup = view.findViewById(R.id.vg_chat_container)
        private val messageLayout: ViewGroup = view.findViewById(R.id.vg_layout_chat_message)
        private val tvChatMessage: TextView = view.findViewById(R.id.tv_chat_msg)
        private val tvChatTime: TextView = view.findViewById(R.id.tv_chat_time)
        private val tvUserName: TextView = view.findViewById(R.id.tv_user_name)
        private val ivUserAvatar: ImageView = view.findViewById(R.id.iv_user_avatar)
        private val tvMessageEdited: TextView = view.findViewById(R.id.tv_message_edited)

        private var messageEntity: MessageEntity? = null

        private val gridLayoutManager = NoScrollableGridLayoutManager(itemView.context, MAX_ROWS).apply {
            initialPrefetchItemCount = 5
        }

        private val rvChatImages = view.findViewById<RecyclerView>(R.id.rv_receiver_recycler).apply {
            layoutManager = gridLayoutManager
            isNestedScrollingEnabled = false
            setHasFixedSize(true)
        }
        private val blurImageContainer: FrameLayout = view.findViewById(R.id.blur_image_container)
        private val tvBtnDisableBlur: TextView = view.findViewById(R.id.tv_btn_disable_image_blur)

        private val fwdContainer: LinearLayout = view.findViewById(R.id.forward_container_receive)
        private val tvFwdAuthorName: TextView = view.findViewById(R.id.tv_fwd_author_name)

        override fun getSwipeContainer() = messageBubbleContainer

        private var message: MessageEntity? = null

        override fun canSwipe() = isMessageSwipeEnabled(message)

        @SuppressLint("SetTextI18n")
        fun bind(
            message: MessageEntity?,
            roomType: String?,
            prevMessage: MessageEntity?,
            nextMessage: MessageEntity?
        ) {
            message?.let { currentMessage ->
                tvMessageEdited.isVisible = isMessageEditEnabled && currentMessage.isEdited()
                this.message = currentMessage
                this.messageEntity = currentMessage
                itemView.tag = message.msgId

                setMessageTime(tvChatTime, message.createdAt)
                setMessageContainerTopMargin(
                    messageLayout,
                    currentMessage,
                    prevMessage
                )

                // User Avatar in a group chat
                if (roomType == ROOM_TYPE_GROUP) {
                    tvUserName.text = message.creator?.name
                    tvUserName.visible()
                    tvChatMessage.setPadding(dpToPx(16), 0, dpToPx(16), dpToPx(8))

                    // Hide user avatar in previous message if message author is the same in a group chat
                    if (message.creator?.userId != nextMessage?.creator?.userId) {
                        ivUserAvatar.visible()
                        ivUserAvatar.loadGlideCircle(message.creator?.avatarSmall)
                    } else {
                        ivUserAvatar.invisible()
                    }
                }

                handleMessageImageAttachments(message)
                handleMessageHeader(itemView.context, messageLayout, currentMessage)
                handleReplyMessage(currentMessage, onMessageClicked)
                highlightMessageScrollAnimation(false, currentMessage, messageBubbleContainer)

                fwdContainer.setMargins(bottom = FWD_CONTAINER_MARGIN_BOTTOM_10.dp)
                handleMessageForwarding(currentMessage, fwdContainer, tvFwdAuthorName, onMessageClicked)

                messageBubbleContainer.setOnLongClickListener(this)
            }
        }

        private fun handleMessageImageAttachments(message: MessageEntity) {
            val attachments = message.attachments
            if (attachments.isNotEmpty()) { //here we can take a several images
                val uris = mutableListOf<String>()
                attachments.forEach {
                    uris.add(it.url)
                }
                if (uris.isNotEmpty()) {
                    rvChatImages.visible()
                    tvChatMessage.measure(0, 0)
                    setNestedImagesRecycler(
                        message = message,
                        recyclerView = rvChatImages,
                        blurContainer = blurImageContainer,
                        gridLayoutManager = gridLayoutManager,
                        images = uris,
                        longClickListener = this,
                        aspectRatio = if (uris.size == 1) message.attachment.ratio else null,
                        minimalWidth = tvChatMessage.measuredWidth,
                        isMyImages = false,
                    )
                    handleEmojis(tvChatMessage, message, false)
                    if (tvChatMessage.text.isNullOrEmpty()) tvChatMessage.gone() else tvChatMessage.visible()
                } else {
                    handleEmojis(tvChatMessage, message, true)
                    handleAttachment(message)
                }
            } else {
                handleEmojis(tvChatMessage, message, false)
                handleAttachment(message)
            }

            tvBtnDisableBlur.click {
                onMessageClicked.disableImageBlur(message) }
        }

        private fun handleAttachment(message: MessageEntity) {
            val attachment = message.attachment
            if ((attachment.type == TYPING_TYPE_IMAGE || attachment.type == TYPING_TYPE_GIF)
                && attachment.url.isNotEmpty()
            ) {
                rvChatImages.visible()
                tvChatMessage.measure(0, 0)
                setNestedImagesRecycler(
                    message = message,
                    recyclerView = rvChatImages,
                    blurContainer = blurImageContainer,
                    gridLayoutManager = gridLayoutManager,
                    images = mutableListOf(attachment.url),
                    longClickListener = this,
                    aspectRatio = attachment.ratio,
                    minimalWidth = tvChatMessage.measuredWidth,
                    isMyImages = false,
                )
            } else {
                rvChatImages.gone()
            }

            tvBtnDisableBlur.click { onMessageClicked.disableImageBlur(message) }
        }

        override fun onClick(p0: View?) {
            /** STUB */
        }

        override fun onLongClick(view: View?): Boolean {
            onMessageClicked.onMessageLongClicked(messageEntity, view)
            return true
        }
    }


    /**
     * Receive deleted message
     */
    inner class ReceiveMessageDeletedHolder(
        view: View,
        private val onMessageClicked: IOnMessageClickedNew
    ) : BaseMessageViewHolder(view) {

        private val messageLayout: LinearLayout = view.findViewById(R.id.layout_chat_message)
        private var tvTitle: TextView = view.findViewById(R.id.tv_delete_message_title_receiver)
        private val createdAt: TextView = view.findViewById(R.id.tv_receiver_created_at)
        private val ivUserAvatar: ImageView = view.findViewById(R.id.iv_user_avatar)

        fun bind(message: MessageEntity?, prevMessage: MessageEntity?, roomType: String?) {
            message?.let { currentMessage ->
                // Title call message deleted
                if (currentMessage.eventCode == ChatEventEnum.CALL.state) {
                    tvTitle.text = itemView.context.getString(R.string.chat_message_deleted_call)
                } else {
                    tvTitle.text = itemView.context.getString(R.string.chat_message_deleted)
                }

                setMessageTime(createdAt, currentMessage.createdAt)
                setMessageContainerTopMargin(
                    messageLayout,
                    currentMessage,
                    prevMessage
                )

                // Avatar
                if (roomType == ROOM_TYPE_GROUP) {
                    ivUserAvatar.visible()
                    ivUserAvatar.loadGlideCircle(currentMessage.creator?.avatarSmall)
                }

                itemView.longClick { onMessageClicked.onDeletedMessageLongClicked(message) }
            }
        }
    }


    /**
     * Receiver IMAGE holder
     */
    inner class ReceiverImageHolder(
        view: View,
        private val onMessageClicked: IOnMessageClickedNew
    ) : BaseMessageViewHolder(view), ISwipeableHolder {

        private val messageBubbleContainer: ViewGroup = view.findViewById(R.id.vg_chat_container)
        private val messageLayout: LinearLayout = view.findViewById(R.id.vg_layout_chat_message)
        private val ivUserAvatar: ImageView = view.findViewById(R.id.iv_user_avatar)
        private val tvImageTime: TextView = view.findViewById(R.id.tv_chat_time)

        private val gridLayoutManager = NoScrollableGridLayoutManager(itemView.context, MAX_ROWS).apply {
            initialPrefetchItemCount = 5
        }

        private val rvChatImages = view.findViewById<RecyclerView>(R.id.rv_chat_images_receiver).apply {
            layoutManager = gridLayoutManager
            isNestedScrollingEnabled = false
            setHasFixedSize(true)
        }

        private val blurImageContainer: FrameLayout = view.findViewById(R.id.blur_image_container)
        private val tvBtnDisableBlur: TextView = view.findViewById(R.id.tv_btn_disable_image_blur)

        private val fwdContainer: LinearLayout = view.findViewById(R.id.forward_container_receive)
        private val tvFwdAuthorName: TextView = view.findViewById(R.id.tv_fwd_author_name)
        private val tvMessageEdited: TextView = view.findViewById(R.id.tv_message_edited)

        private var message: MessageEntity? = null

        override fun canSwipe() = isMessageSwipeEnabled(message)

        override fun getSwipeContainer() = messageBubbleContainer


        fun bind(message: MessageEntity?, roomType: String?, prevMessage: MessageEntity?) {
            message?.let { currentMessage ->
                this.message = currentMessage
                tvMessageEdited.isVisible = isMessageEditEnabled && currentMessage.isEdited()
                itemView.tag = message.msgId

                setMessageTime(tvImageTime, message.createdAt)
                setMessageContainerTopMargin(
                    messageLayout,
                    currentMessage,
                    prevMessage
                )
                handleMessageHeader(itemView.context, messageLayout, currentMessage)
                handleReplyMessage(currentMessage, onMessageClicked)
                highlightMessageScrollAnimation(false, currentMessage, messageBubbleContainer)

                // Avatar
                if (roomType == ROOM_TYPE_GROUP) {
                    ivUserAvatar.visible()
                    ivUserAvatar.loadGlideCircle(message.creator?.avatarSmall)
                }

                val img = mutableListOf<String>()
                if (message.attachments.isNotEmpty()) {
                    message.attachments.forEach {
                        img.add(it.url)
                    }
                } else {
                    img.add(message.attachment.url)
                }

                initImage(img, message)

                fwdContainer.setMargins(bottom = FWD_CONTAINER_MARGIN_BOTTOM_8.dp)
                handleMessageForwarding(currentMessage, fwdContainer, tvFwdAuthorName, onMessageClicked)

                messageBubbleContainer.setOnLongClickListener { false }

                tvBtnDisableBlur.click { onMessageClicked.disableImageBlur(message) }
            }
        }

        private fun initImage(img: List<String>, message: MessageEntity?) {
            message?.let {
                rvChatImages.visible()
                setNestedImagesRecycler(
                    message = message,
                    recyclerView = rvChatImages,
                    blurContainer = blurImageContainer,
                    gridLayoutManager = gridLayoutManager,
                    images = img,
                    longClickListener = {
                        onMessageClicked.onMessageLongClicked(message, it); true
                    },
                    aspectRatio = if (img.size == 1) message.attachment.ratio else null,
                    isShowGiphyWatermark = message.isShowGiphyWatermark ?: false,
                    isMyImages = false,
                )
            }
        }
    }

    var isNewYearStyleEnabled: Boolean = false

    /**
     * Handle service messages
     */
    inner class ServiceMessageHolder(view: View) : BaseMessageViewHolder(view),
        View.OnClickListener,
        View.OnLongClickListener {

        private val tvServiceMessage: TextView = view.findViewById(R.id.tv_service_text)

        fun bind(message: MessageEntity?) {
            message?.let { currentMessage ->
                tvServiceMessage.text = currentMessage.content
            }
        }

        override fun onClick(view: View?) = Unit

        override fun onLongClick(view: View?): Boolean = true
    }

    /**
     * View holder for call message
     * - calling - начался вызов
     * - accepted - вызов состоялся, происходит в текущий момент
     * - rejected - вызов отклонен пользователем, который получил звонок
     * - declined - вызов отклонен инициатором звонка
     * - missed - пропущен
     * - stopped - звонок состоялся и завершен
     */
    inner class CallMessageViewHolder(
        view: View,
        private val myUid: Long,
        private val onMessageClicked: IOnMessageClickedNew
    ) : BaseMessageViewHolder(view), View.OnLongClickListener {

        private val ctx = view.context

        private val messageLayout: LinearLayout = view.findViewById(R.id.layout_chat_message)

        // Incoming call
        private val containerIncomingCall: ConstraintLayout = view.findViewById(R.id.container_incoming)
        private val ivTypeCallIncoming: ImageView = view.findViewById(R.id.iv_type_of_call_incoming)
        private val tvDescriptionIncoming: TextView = view.findViewById(R.id.tv_call_description_incoming)
        private val tvCallTimeIncoming: TextView = view.findViewById(R.id.tv_call_time_incoming)

        // Outgoing call
        private val containerOutgoingCall: ConstraintLayout = view.findViewById(R.id.container_outgoing)
        private val ivTypeCallOutgoing: ImageView = view.findViewById(R.id.iv_type_of_call_outgoing)
        private val tvDescriptionOutgoing: TextView = view.findViewById(R.id.tv_call_description_outgoing)
        private val tvCallTimeOutgoing: TextView = view.findViewById(R.id.tv_call_time_outgoing)

        private val ivMarkerSent: ImageView = view.findViewById(R.id.iv_call_outgoing_marker_sent)
        private val ivMarkerDelivered: ImageView = view.findViewById(R.id.iv_call_outgoing_marker_delivered)
        private val ivMarkerRead: ImageView = view.findViewById(R.id.iv_call_outgoing_marker_read)

        private var messageEntity: MessageEntity? = null


        fun bind(message: MessageEntity?, prevMessage: MessageEntity?) {
            // Timber.e("BIND Call message ===> (CallerId) ${message?.metadata?.caller?.callerId}")
            message?.let { currentMessage ->
                this.messageEntity = currentMessage

                val isIncoming = currentMessage.metadata?.caller?.callerId != myUid
                when (currentMessage.metadata?.status) {
                    CallStatusEnum.CALLING.status -> Timber.e("Status -> Calling")
                    CallStatusEnum.ACCEPTED.status -> Timber.e("Status -> Accepted")
                    CallStatusEnum.REJECTED.status, CallStatusEnum.DECLINED.status, CallStatusEnum.MISSED.status -> {
                        val isRejectedDeclined = currentMessage.metadata?.status == CallStatusEnum.REJECTED.status
                            || currentMessage.metadata?.status == CallStatusEnum.DECLINED.status
                        val text = if (!isIncoming) {
                            ctx.getText(R.string.canceled_call)
                        } else {
                            ctx.getString(R.string.missed_call_txt)
                        }
                        if (isIncoming) {
                            callMessagesSelector(
                                isIncoming,
                                currentMessage,
                                text.toString(),
                                isRejected = isRejectedDeclined
                            )
                        } else {
                            callMessagesSelector(
                                isIncoming,
                                currentMessage,
                                text.toString(),
                                isRejected = isRejectedDeclined
                            )
                        }
                    }
                    CallStatusEnum.STOPPED.status -> callMessagesSelector(
                        isIncoming,
                        currentMessage,
                        ctx.getString(R.string.call_status_stopped),
                        isShowDuration = true
                    )
                    else -> messageLayout.gone()
                }

                if (prevMessage != null) {
                    val messageContainer = if (isIncoming) containerIncomingCall else containerOutgoingCall
                    setMarginTopContainer(messageContainer, currentMessage, prevMessage)
                }

                // Message status
                ivMarkerSent.visible()
                handleMessageStatus(
                    ivMarkerDelivered,
                    ivMarkerRead,
                    ivMarkerSent,
                    message.delivered,
                    message.readed,
                    message.sent
                )

                containerIncomingCall.setOnLongClickListener(this)
                containerOutgoingCall.setOnLongClickListener(this)
            }

            changeDividerStyle()
        }

        // In design (Mockups)
        private fun setMarginTopContainer(
            container: ConstraintLayout,
            currMessage: MessageEntity,
            prevMessage: MessageEntity
        ) {
            if (currMessage.creator?.userId != prevMessage.creator?.userId) {
                container.setMargins(null, dpToPx(MARGIN_TOP_OPPONENT_ITEMS), null, null)
            } else {
                container.setMargins(null, dpToPx(MARGIN_TOP_USER_ITEMS), null, null)
            }
        }

        private fun callMessagesSelector(
            isIncoming: Boolean,
            message: MessageEntity,
            description: String,
            isRejected: Boolean = false,
            isShowDuration: Boolean = false
        ) {
            if (isIncoming) {
                showIncomingMessage(message, description, isRejected, isShowDuration)
            } else
                showOutgoingMessage(message, description, isRejected, isShowDuration)
        }


        private fun showIncomingMessage(
            message: MessageEntity,
            description: String,
            isRejected: Boolean,
            isShowDuration: Boolean
        ) {
            containerIncomingCall.visible()
            containerOutgoingCall.gone()
            if (isRejected) {
                ivTypeCallIncoming.setImageDrawable(ctx.getDrawable(R.drawable.icon_call_rejected))
            }
            if (isShowDuration) {
                val durationStr = getDurationSeconds(message.metadata?.callDuration ?: 0)
                tvDescriptionIncoming.text = ctx.getString(R.string.call_status_call_time, durationStr)
                // gray text
                tvDescriptionIncoming.setTextColor(Color.parseColor("#7f7f7f"))
            } else {
                Timber.d("tvDescriptionIncoming = $description")
                tvDescriptionIncoming.text = description
            }
            tvCallTimeIncoming.text = getShortTime(
                message.metadata?.createdAt ?: 0,
                DateFormat.is24HourFormat(ctx.applicationContext)
            )
        }


        private fun showOutgoingMessage(
            message: MessageEntity,
            description: String,
            isRejected: Boolean,
            isShowDuration: Boolean
        ) {
            containerOutgoingCall.visible()
            containerIncomingCall.gone()
            if (isRejected) {
                ivTypeCallOutgoing.setImageDrawable(ctx.getDrawable(R.drawable.icon_call_rejected))
            }
            if (isShowDuration) {
                val durationStr = getDurationSeconds(message.metadata?.callDuration ?: 0)
                tvDescriptionOutgoing.text = ctx.getString(R.string.call_status_call_time, durationStr)
            } else {
                Timber.d("tvDescriptionOutgoing = $description")
                tvDescriptionOutgoing.text = description
            }
            tvCallTimeOutgoing.text = getShortTime(
                message.metadata?.createdAt ?: 0,
                DateFormat.is24HourFormat(ctx.applicationContext)
            )
        }


        override fun onLongClick(v: View?): Boolean {
            onMessageClicked.onMessageLongClicked(messageEntity, v)
            return true
        }
    }

    private fun setNestedImagesRecycler(
        message: MessageEntity?,
        recyclerView: RecyclerView,
        blurContainer: FrameLayout?,
        gridLayoutManager: GridLayoutManager,
        images: List<String>,
        longClickListener: View.OnLongClickListener? = null,
        aspectRatio: Double? = null,
        isShowGiphyWatermark: Boolean = false,
        minimalWidth: Int = -1,
        isMyImages: Boolean,
    ) {
        val rvMargins = recyclerView.marginStart + recyclerView.marginEnd
        val maxContainerWidth = (screenWidth * MESSAGE_WIDTH_RELATIVE).toInt() - dpToPx(MESSAGE_SIDE_MARGIN) - rvMargins
        val containerSizes = calculateRecyclerSizes(
            maxContainerSize = maxContainerWidth,
            itemsCount = images.size,
            aspectRatio = aspectRatio,
        )
        val preferredWidth = min(maxContainerWidth, max(containerSizes.width, max(minimalWidth, dpToPx(MIN_IMAGE_WIDTH))))
        val completePreferredWidth = calculateCompleteRecyclerWidth(maxContainerWidth, preferredWidth, images)

        val preferredHeight = max(containerSizes.height, dpToPx(MIN_IMAGE_HEIGHT))
        recyclerView.layoutParams.height = preferredHeight
        recyclerView.layoutParams.width = completePreferredWidth
        Timber.d("maxContainerWidth: $maxContainerWidth, " +
            "containerSizes.width: ${containerSizes.width}, " +
            "minimalWidth: $minimalWidth, " +
            "preferredWidth: $preferredWidth, "
        )

        val isBlurImage = message?.isShowImageBlurChatRequest ?: false
        if (isBlurImage) {
            Timber.d("isShowImageBlurChatRequest: true")
            blurContainer?.visible()
            if (containerSizes.height < dpToPx(BLUR_OVERLAY_CONTAINER_HEIGHT_MIN)) {
                blurContainer?.findViewById<TextView>(R.id.tv_blur_description)?.gone()
            } else {
                blurContainer?.findViewById<TextView>(R.id.tv_blur_description)?.visible()
            }
        } else {
            Timber.d("isShowImageBlurChatRequest: false")
            blurContainer?.gone()
        }

        val adapter = ChatImagesAdapter(
            act = act,
            message = message,
            blurHelper = blurHelper,
            callback = onMessageClicked,
            longClickListener = longClickListener,
            containerWidth = completePreferredWidth,
            isMyImages = isMyImages,
        ) // адаптер нужно создавать каждый раз новый
        gridLayoutManager.spanSizeLookup = adapter.lookup
        recyclerView.adapter = adapter
        adapter.setList(images.map { url -> PostImage(url = url, isShowGiphyWatermark = isShowGiphyWatermark) })
    }

    private fun calculateCompleteRecyclerWidth(
        maxContainerWidth: Int,
        prepWidth: Int,
        images: List<String>
    ): Int {
        return if (images.size > 1) {
            getPreparedRecyclerWidthForManyImages(prepWidth)
        } else {
            getPreparedRecyclerWidthForSingleImage(maxContainerWidth, prepWidth)
        }
    }

    private fun getPreparedRecyclerWidthForManyImages(prepWidth: Int) =
        prepWidth - dpToPx(MEDIA_RECYCLER_WIDTH_OFFSET)

    private fun getPreparedRecyclerWidthForSingleImage(
        maxContainerWidth: Int,
        prepWidth: Int
    ): Int {
        return if (maxContainerWidth - prepWidth < HORIZONTAL_MEDIA_RANGE_SIZE) {
            prepWidth - dpToPx(MEDIA_RECYCLER_WIDTH_OFFSET)
        } else {
            prepWidth
        }
    }

    private fun calculateRecyclerSizes(
        maxContainerSize: Int,
        itemsCount: Int,
        aspectRatio: Double?,
    ): Size {
        val effectiveAspectRatio = if (aspectRatio == null || aspectRatio == (-1).toDouble()) 1.0 else aspectRatio
        val recommendedHeight = when (itemsCount) {
            1 -> (maxContainerSize / effectiveAspectRatio).toInt().coerceAtMost(maxContainerSize)
            2 -> maxContainerSize
            3 -> (maxContainerSize * 1.2f).toInt()
            4 -> (maxContainerSize * 1.4f).toInt()
            5 -> (maxContainerSize * 1.5f).toInt()
            else -> error("Please describe new scenario.")
        }
        val recommendedWidth = when (itemsCount) {
            1 -> (recommendedHeight * effectiveAspectRatio).toInt().coerceAtMost(maxContainerSize)
            else -> maxContainerSize
        }
        Timber.d(
            "maxContainerSize: $maxContainerSize, " +
                "aspectRatio: $aspectRatio, " +
                "recommendedHeight: $recommendedHeight, " +
                "recommendedWidth: $recommendedWidth, "
        )
        return Size(recommendedWidth, recommendedHeight)
    }

    ///////////////////////////////////////////////////////////////////////////
    // Gifts holders
    ///////////////////////////////////////////////////////////////////////////


    inner class SenderGiftViewHolder(
        view: View,
        private val onMessageClicked: IOnMessageClickedNew
    ) : BaseMessageViewHolder(view), View.OnClickListener {

        private val messageLayout: ViewGroup = view.findViewById(R.id.layout_chat_message)
        private val tvChatMessage: TextView = view.findViewById(R.id.tv_chat_msg)
        private val tvChatTime: TextView = view.findViewById(R.id.tv_chat_time)
        private val ivMarkerSent: ImageView = view.findViewById(R.id.iv_marker_sent)
        private val ivMarkerDelivered: ImageView = view.findViewById(R.id.iv_marker_delivered)
        private val ivMarkerRead: ImageView = view.findViewById(R.id.iv_marker_read)
        private val llMessageBubble: LinearLayout? = view.findViewById(R.id.ll_chat_bubble_background)
        private val tvGiftTitle = itemView.findViewById<TextView>(R.id.tv_gift_title)

        private val ivGift: ImageView = view.findViewById(R.id.iv_gift_sender)

        private var messageEntity: MessageEntity? = null


        fun bind(message: MessageEntity?, prevMessage: MessageEntity?) {
            message?.let { currentMessage ->
                this.messageEntity = currentMessage

                setMessageContainerTopMargin(
                    messageLayout,
                    currentMessage,
                    prevMessage
                )
                if (message.content.isEmpty()) {
                    tvChatMessage.gone()
                } else {
                    tvChatMessage.text = message.content
                    tvChatMessage.visible()
                }
                setMessageTime(tvChatTime, message.createdAt)
                handleMessageStatus(
                    ivMarkerDelivered,
                    ivMarkerRead,
                    ivMarkerSent,
                    message.delivered,
                    message.readed,
                    message.sent
                )

                val typeId = getGiftTypeId(messageEntity?.attachment?.metadata)
                val customTitle = getGiftTitle(messageEntity?.attachment?.metadata)
                val isHolidayTypeId = typeId == TYPE_GIFT_HOLIDAY || typeId == TYPE_GIFT_HOLIDAY_NEW_YEAR
                if (customTitle != null && isHolidayTypeId) {
                    tvGiftTitle.text = customTitle
                }

                ivGift.loadGlide(currentMessage.attachment.url)
                llMessageBubble?.setOnClickListener(this)
                ivGift.setOnClickListener(this)
                ivGift.setOnLongClickListener {
                    onMessageClicked.onMessageLongClicked(message, tvChatMessage)
                    true
                }
                tvChatMessage.setOnLongClickListener {
                    onMessageClicked.onMessageLongClicked(message, tvChatMessage)
                    true
                }
            }
        }

        override fun onClick(v: View?) {
            onMessageClicked.onSenderGiftClicked()
        }

    }

    inner class ReceiverGiftViewHolder(
        view: View,
        private val onMessageClicked: IOnMessageClickedNew
    ) : BaseMessageViewHolder(view), View.OnClickListener {

        private val clGiftContainer: ViewGroup? = view.findViewById(R.id.clGiftContainer)
        private val clickAreaSendGift: View? = view.findViewById(R.id.clickAreaSendGift)
        private val messageLayout: ViewGroup = view.findViewById(R.id.layout_chat_message)
        private val tvChatMessage: TextView = view.findViewById(R.id.tv_chat_msg)
        private val tvChatTime: TextView = view.findViewById(R.id.tv_chat_time)
        private val tvGoToGift = itemView.findViewById<TextView>(R.id.tv_go_to_gift)
        private val tvGiftTitle = itemView.findViewById<TextView>(R.id.tv_gift_title)
        private val llMessageBubble: LinearLayout? = view.findViewById(R.id.ll_chat_bubble_background)

        private val ivGift: ImageView = view.findViewById(R.id.iv_gift_receiver)

        private var messageEntity: MessageEntity? = null

        fun bind(message: MessageEntity?, prevMessage: MessageEntity?) {
            message?.let { currentMessage ->
                this.messageEntity = currentMessage
                messageEntity?.attachment?.metadata
                if (message.content.isEmpty()) {
                    tvChatMessage.gone()
                } else {
                    tvChatMessage.text = message.content
                    tvChatMessage.visible()
                }

                tvGoToGift.visible()
                clickAreaSendGift?.click {
                    onMessageClicked.onChooseGiftClicked(currentMessage.creator ?: UserChat())
                }

                val typeId = getGiftTypeId(messageEntity?.attachment?.metadata)
                val isTypeIdHoliday = typeId == TYPE_GIFT_HOLIDAY || typeId == TYPE_GIFT_HOLIDAY_NEW_YEAR
                if (isTypeIdHoliday) {
                    setupHolidayGift()
                }
                setMessageTime(tvChatTime, message.createdAt)
                setMessageContainerTopMargin(
                    messageLayout,
                    currentMessage,
                    prevMessage
                )
                handleMessageHeader(itemView.context, messageLayout, currentMessage)

                ivGift.loadGlide(currentMessage.attachment.url)
                clGiftContainer?.setOnClickListener(this)
                llMessageBubble?.setOnClickListener(this)
                tvChatMessage.setOnClickListener(this)
                ivGift.setOnClickListener(this)
                ivGift.longClick {
                    onMessageClicked.onMessageLongClicked(message, tvChatMessage)
                }

                tvChatMessage.longClick {
                    onMessageClicked.onMessageLongClicked(message, it)
                }
            }
        }

        private fun setupHolidayGift() {
            val customTitle = getGiftTitle(messageEntity?.attachment?.metadata)
            if (customTitle != null) {
                tvGiftTitle.text = customTitle
            }
            if (messageEntity?.creator?.userId == NOOMEERA_ACCOUNT_ID) {
                tvGoToGift.gone()
                clickAreaSendGift?.gone()
            } else {
                tvGoToGift.visible()
                clickAreaSendGift?.visible()
            }
        }

        override fun onClick(v: View?) {
            onMessageClicked.onReceiverGiftClicked(myUid)
        }

    }


    // -----------------------------------------------------------------------------

    /**
     * Empty view holder for null items (Sometimes happen bug)
     */
    inner class EmptyViewHolder(view: View) : BaseMessageViewHolder(view) {
        fun bind() {
            /** STUB */
        }
    }

    /**
     * View Holders for messages with video
     */
    inner class ReceiverVideoViewHolder(
        view: View,
        private val clickListener: IOnMessageClickedNew
    ) : BaseMessageViewHolder(view), ISwipeableHolder {

        private val messageBubbleContainer: LinearLayout = view.findViewById(R.id.ll_chat_container)
        private val tvContent: TextView = itemView.findViewById(R.id.tv_chat_msg)
        private val messageLayout: LinearLayout = itemView.findViewById(R.id.layout_chat_message)
        private val blurBackground: View = view.findViewById(R.id.view_narrow_images)
        private val ivImageOfVideo: ImageView = itemView.findViewById(R.id.iv_image_of_video)
        private val videoDurationContainer: View = itemView.findViewById(R.id.repost_video_duration)
        private val ivUserAvatar: ImageView = itemView.findViewById(R.id.iv_user_avatar)
        private val tvImageTime: TextView = itemView.findViewById(R.id.tv_chat_time)
        private val tvImageTimeTxt: TextView = itemView.findViewById(R.id.tv_chat_time_txt)

        private val fwdContainer: LinearLayout = view.findViewById(R.id.forward_container_receive)
        private val tvFwdAuthorName: TextView = view.findViewById(R.id.tv_fwd_author_name)

        private val semitransparentContainer: FrameLayout = view.findViewById(R.id.blur_image_container)
        private val tvBtnDisableBlur: TextView = view.findViewById(R.id.tv_btn_disable_image_blur)
        private val tvMessageEditedTxt: TextView = view.findViewById(R.id.tv_message_edited_txt_mode)
        private val tvMessageEdited: TextView = view.findViewById(R.id.tv_message_edited)

        private var message: MessageEntity? = null

        override fun canSwipe() = isMessageSwipeEnabled(message)

        override fun getSwipeContainer() = messageBubbleContainer

        fun bind(message: MessageEntity?, roomType: String?, prevMessage: MessageEntity?) {
            message?.let { currentMessage ->
                this.message = currentMessage
                itemView.tag = message.msgId

                if (message.content.isEmpty()) {
                    tvMessageEdited.isVisible = isMessageEditEnabled && message.isEdited()
                    tvContent.gone()
                    tvImageTime.visible()
                    tvImageTimeTxt.gone()
                    setMessageTime(tvImageTime, message.createdAt)
                } else {
                    tvMessageEditedTxt.isVisible = isMessageEditEnabled && message.isEdited()
                    tvContent.visible()
                    tvImageTime.gone()
                    tvImageTimeTxt.visible()
                    setMessageTime(tvImageTimeTxt, message.createdAt)
                }

                setMessageContainerTopMargin(
                    messageLayout,
                    currentMessage,
                    prevMessage
                )
                handleMessageHeader(itemView.context, messageLayout, currentMessage)

                // Avatar
                if (roomType == ROOM_TYPE_GROUP) {
                    ivUserAvatar.visible()
                    ivUserAvatar.loadGlideCircle(message.creator?.avatarSmall)
                }
                handleEmojis(tvContent, currentMessage)
                handleReplyMessage(currentMessage, onMessageClicked)
                highlightMessageScrollAnimation(false, currentMessage, messageBubbleContainer)

                message.attachment.makeMetaMessageWithVideo()?.let {
                    val isBlurImage = message.isShowImageBlurChatRequest ?: false
                    if (isBlurImage) {
                        semitransparentContainer.visible()
                        blurBackground.bgrColor(R.color.ui_blur_image_card)
                        blurHelper.blurByUrl(it.preview) { bitmap ->
                            ivImageOfVideo.loadGlideWithCacheAndError(bitmap)
                        }
                    } else {
                        semitransparentContainer.gone()
                        if (currentMessage.content.isNotEmpty()) {
                            blurBackground.bgrColor(R.color.ui_purple_super_light)
                        } else {
                            blurBackground.bgrColor(R.color.ui_color_chat_send_grey)
                        }
                        ivImageOfVideo.loadGlideWithCacheAndError(it.preview)
                    }

                    if (it.duration.toInt() != 0) {
                        val durationText = getDurationSeconds(it.duration.toInt())
                        videoDurationContainer.visible()
                        val time = videoDurationContainer.findViewById<TextView>(R.id.exo_position)
                        time.text = durationText
                    } else {
                        videoDurationContainer.gone()
                    }
                }

                fwdContainer.setMargins(bottom = FWD_CONTAINER_MARGIN_BOTTOM_10.dp)
                handleMessageForwarding(currentMessage, fwdContainer, tvFwdAuthorName, onMessageClicked)

                tvBtnDisableBlur.click { onMessageClicked.disableImageBlur(message) }
                messageLayout.click { clickListener.onMessageLongClicked(message) }
                ivImageOfVideo.click { onMessageClicked.onAttachmentClicked(message) }
                ivImageOfVideo.longClick { onMessageClicked.onMessageLongClicked(message, tvContent) }

                messageBubbleContainer.setOnLongClickListener {
                    onMessageClicked.onMessageLongClicked(message, tvContent)
                    true
                }
                val builder = Zoomy.Builder(act)
                    .target(ivImageOfVideo)
                    .interpolator(OvershootInterpolator())
                    .tapListener {
                        onMessageClicked.onAttachmentClicked(message)
                    }
                    .longPressListener {
                        onMessageClicked.onMessageLongClicked(message, tvContent)
                    }
                builder.register()
            }
        }

        private fun View.bgrColor(colorRes: Int) {
            setBackgroundColor(act.color(colorRes))
        }
    }

    inner class SenderVideoViewHolder(view: View) : BaseMessageViewHolder(view), ISwipeableHolder {

        private val llBubble: LinearLayout = itemView.findViewById(R.id.ll_chat_bubble_background)
        private val messageContainer: FrameLayout = view.findViewById(R.id.only_message_bubble_container)

        private val messageLayout: ViewGroup = itemView.findViewById(R.id.layout_chat_message)
        private val ivImageOfVideo: ImageView = itemView.findViewById(R.id.iv_image_of_video)
        private val videoDurationContainer: View = itemView.findViewById(R.id.repost_video_duration)
        private val time = videoDurationContainer.findViewById<TextView>(R.id.exo_position)
        private val tvContent: TextView = itemView.findViewById(R.id.tv_chat_msg)

        private val statusContainer: LinearLayout = itemView.findViewById(R.id.vg_status_container)
        private val tvChatTime: TextView = itemView.findViewById(R.id.tv_chat_time)
        private val ivSent: ImageView = itemView.findViewById(R.id.iv_marker_sent)
        private val ivDelivered: ImageView = itemView.findViewById(R.id.iv_marker_delivered)
        private val ivRead: ImageView = itemView.findViewById(R.id.iv_marker_read)

        private val statusContainerTxt: LinearLayout = itemView.findViewById(R.id.vg_status_container_txt_mode)
        private val tvChatTimeTxt: TextView = itemView.findViewById(R.id.tv_chat_time_txt_mode)
        private val ivSentTxt: ImageView = itemView.findViewById(R.id.iv_marker_sent_txt_mode)
        private val ivDeliveredTxt: ImageView = itemView.findViewById(R.id.iv_marker_delivered_txt_mode)
        private val ivReadTxt: ImageView = itemView.findViewById(R.id.iv_marker_read_txt_mode)

        private val ivResendError: ImageView = itemView.findViewById(R.id.iv_sender_simple_image_message_error)
        private val resendTapContainer: FrameLayout = view.findViewById(R.id.resend_tap_container)
        private val pbProgress: ProgressBar = view.findViewById(R.id.pb_progress)

        private val fwdContainer: LinearLayout = view.findViewById(R.id.forward_container_send)
        private val tvFwdAuthorName: TextView = view.findViewById(R.id.tv_fwd_author_name)
        private val tvMessageEditedTxt: TextView = view.findViewById(R.id.tv_message_edited_txt_mode)
        private val tvMessageEdited: TextView = view.findViewById(R.id.tv_message_edited)

        private var messageEntity: MessageEntity? = null

        override fun canSwipe() = isMessageSwipeEnabled(messageEntity)

        override fun getSwipeContainer() = llBubble


        fun bind(message: MessageEntity?, prevMessage: MessageEntity?) {
            message?.let { newMessage ->
                itemView.tag = newMessage.msgId
                this.messageEntity = newMessage
                val isTextNotExists = newMessage.content.isEmpty()

                val layoutParams = getBubbleLayoutParams(newMessage, isTextNotExists)
                layoutParams.gravity = Gravity.END
                llBubble.layoutParams = layoutParams

                statusContainer.visible()
                setVideoPreview(
                    message = message,
                    onComplete = {}
                )
                showStatusBlock(message, isTextNotExists = isTextNotExists)

                setMessageContainerTopMargin(
                    messageLayout,
                    newMessage,
                    prevMessage
                )
                highlightMessageScrollAnimation(true, newMessage, messageContainer)
                handleReplyMessage(newMessage, onMessageClicked)

                if (message.content.isNotEmpty()) tvContent.visible()
                handleEmojis(tvContent, newMessage)

                fwdContainer.setMargins(bottom = FWD_CONTAINER_MARGIN_BOTTOM_10.dp)
                handleMessageForwarding(newMessage, fwdContainer, tvFwdAuthorName, onMessageClicked)

                ivImageOfVideo.click { onMessageClicked.onAttachmentClicked(messageEntity) }
                ivImageOfVideo.longClick { onMessageClicked.onMessageLongClicked(messageEntity, tvContent) }
                llBubble.longClick {
                    onMessageClicked.onMessageLongClicked(messageEntity, tvContent)
                }
                val builder = Zoomy.Builder(act)
                    .target(ivImageOfVideo)
                    .interpolator(OvershootInterpolator())
                    .tapListener {
                        onMessageClicked.onAttachmentClicked(messageEntity)
                    }
                    .longPressListener {
                        onMessageClicked.onMessageLongClicked(messageEntity, tvContent)
                    }
                builder.register()
            }
        }

        private fun setVideoPreview(message: MessageEntity?, onComplete: () -> Unit) {
            val attachmentMeta = message?.attachment?.makeMetaMessageWithVideo()

            if (attachmentMeta != null) {
                loadPreview(attachmentMeta.preview, onComplete)

                if (attachmentMeta.duration.toInt() != 0) {
                    val durationText = getDurationSeconds(attachmentMeta.duration.toInt())
                    videoDurationContainer.visible()
                    time.text = durationText
                } else {
                    videoDurationContainer.invisible()
                }
            } else {
                loadPreview(message?.attachment?.url ?: String.empty(), onComplete)
            }
        }

        private fun showStatusBlock(message: MessageEntity, isTextNotExists: Boolean) {
            if (isTextNotExists) {
                tvMessageEdited.isVisible = isMessageEditEnabled && message.isEdited()
                tvContent.invisible()
                statusContainerTxt.invisible()
                setMessageTime(tvChatTime, message.createdAt)
                handleMessageStatus(
                    ivDelivered,
                    ivRead,
                    ivSent,
                    message.delivered,
                    message.readed,
                    message.sent
                )
                handleActionProgress(
                    ivResendError,
                    ivSent,
                    llBubble,
                    resendTapContainer,
                    message,
                    pbProgress = pbProgress
                )
            } else {
                tvMessageEditedTxt.isVisible = isMessageEditEnabled && message.isEdited()
                tvContent.visible()
                statusContainer.invisible()
                statusContainerTxt.visible()
                setMessageTime(tvChatTimeTxt, message.createdAt)
                handleMessageStatus(
                    ivDeliveredTxt,
                    ivReadTxt,
                    ivSentTxt,
                    message.delivered,
                    message.readed,
                    message.sent
                )
                handleActionProgress(
                    ivResendError,
                    ivSentTxt,
                    llBubble,
                    resendTapContainer,
                    message,
                    pbProgress = pbProgress
                )
            }
        }

        private fun loadPreview(url: String, onComplete: () -> Unit) {
            Glide.with(ivImageOfVideo.context)
                .load(url)
                .transition(DrawableTransitionOptions.withCrossFade())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        ivImageOfVideo.setImageDrawable(R.drawable.no_chat_media_placeholder)
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: Target<Drawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        onComplete.invoke()
                        return false
                    }
                })
                .into(ivImageOfVideo)
        }

        private fun getBubbleLayoutParams(
            message: MessageEntity,
            isTextNotExists: Boolean
        ): FrameLayout.LayoutParams {
            return if (isTextNotExists) {
                return getBubbleLayoutParamsOnlyVideo(message)
            } else {
                FrameLayout.LayoutParams(maxBubbleWidth, WRAP_CONTENT)
            }
        }

        private fun getBubbleLayoutParamsOnlyVideo(message: MessageEntity): FrameLayout.LayoutParams {
            val aspectRatio = message.attachment.ratio
            return if (aspectRatio > 1) {
                val height = (maxBubbleWidth / aspectRatio).toInt()
                FrameLayout.LayoutParams(maxBubbleWidth, height)
            } else {
                FrameLayout.LayoutParams(maxBubbleWidth, dpToPx(DEFAULT_MEDIA_CONTAINER_HEIGHT))
            }
        }
    }

    inner class SenderShareProfileViewHolder(view: View) : BaseMessageViewHolder(view), ISwipeableHolder {

        private val llMessageBubbleContainer: LinearLayout =
            view.findViewById(R.id.ll_message_bubble_container)
        private val messageContainer: FrameLayout =
            view.findViewById(R.id.vg_frame_layout_only_message_bubble_container)
        private val messageLayout: ViewGroup = view.findViewById(R.id.layout_chat_message)

        private val ivAvatar: ImageView = view.findViewById(R.id.iv_share_profile_avatar_send)
        private val tvName: TextView = view.findViewById(R.id.tv_share_profile_name_send)
        private val tvUniqueName: TextView = view.findViewById(R.id.tv_share_profile_uniquename_send)
        private val llShareProfileInfo: FrameLayout = view.findViewById(R.id.fl_share_profile_info)
        private val ivGender: ImageView = view.findViewById(R.id.iv_share_profile_gender_send)
        private val tvAgeAddress: TextView = view.findViewById(R.id.tv_share_profile_age_address_send)
        private val tvChatTime: TextView = view.findViewById(R.id.tv_chat_time)

        private val ivMarkerSent: ImageView = view.findViewById(R.id.iv_marker_sent)
        private val ivMarkerDelivered: ImageView = view.findViewById(R.id.iv_marker_delivered)
        private val ivMarkerRead: ImageView = view.findViewById(R.id.iv_marker_read)

        private val fwdContainer: LinearLayout = view.findViewById(R.id.forward_container_send)
        private val tvFwdAuthorName: TextView = view.findViewById(R.id.tv_fwd_author_name)
        private val tvMessageEdited: TextView = view.findViewById(R.id.tv_message_edited)

        fun bind(message: MessageEntity?, prevMessage: MessageEntity?) {
            message?.let { currentMessage ->
                tvMessageEdited.isVisible = isMessageEditEnabled && currentMessage.isEdited()
                val context = itemView.context

                fwdContainer.setMargins(bottom = FWD_CONTAINER_MARGIN_BOTTOM_8.dp)
                val isForwarding =
                    handleMessageForwarding(currentMessage, fwdContainer, tvFwdAuthorName, onMessageClicked)

                handleShareProfileMessage(
                    message = currentMessage,
                    ivAvatar = ivAvatar,
                    tvName = tvName,
                    tvUniqueName = tvUniqueName,
                    flShareProfileInfo = llShareProfileInfo,
                    ivGender = ivGender,
                    tvAgeAddress = tvAgeAddress,
                    clickListener = onMessageClicked,
                    isSenderMessage = !isForwarding
                )

                setMessageTime(tvChatTime, message.createdAt)
                handleMessageHeader(context, messageLayout, currentMessage)
                handleMessageStatus(
                    ivMarkerDelivered,
                    ivMarkerRead,
                    ivMarkerSent,
                    message.delivered,
                    message.readed,
                    message.sent
                )
                setMessageContainerTopMargin(
                    messageLayout,
                    currentMessage,
                    prevMessage
                )
                handleReplyMessage(currentMessage, onMessageClicked)
                highlightMessageScrollAnimation(
                    true, currentMessage, messageContainer
                )
                // 30.08.2021 - поделиться профилем в групповой чат на данном этапе нельзя
            }
        }

        override fun getSwipeContainer() = llMessageBubbleContainer

    }

    inner class ReceiverShareProfileViewHolder(view: View) : BaseMessageViewHolder(view), ISwipeableHolder {

        private val llMessageBubbleContainer: FrameLayout =
            view.findViewById(R.id.whole_message_width_container)
        private val messageLayout: ViewGroup = view.findViewById(R.id.layout_chat_message)

        private val ivAvatar: ImageView = view.findViewById(R.id.iv_share_profile_avatar_receive)
        private val tvName: TextView = view.findViewById(R.id.tv_share_profile_name_receive)
        private val tvUniqueName: TextView = view.findViewById(R.id.tv_share_profile_uniquename_receive)
        private val llShareProfileInfo: FrameLayout = view.findViewById(R.id.fl_share_profile_info)
        private val ivGender: ImageView = view.findViewById(R.id.iv_share_profile_gender_receive)
        private val tvAgeAddress: TextView = view.findViewById(R.id.tv_share_profile_age_address_receive)
        private val tvChatTime: TextView = view.findViewById(R.id.tv_chat_time)
        private val fwdContainer: LinearLayout = view.findViewById(R.id.forward_container_receive)
        private val tvFwdAuthorName: TextView = view.findViewById(R.id.tv_fwd_author_name)


        fun bind(message: MessageEntity?, prevMessage: MessageEntity?) {
            message?.let { currentMessage ->
                val context = itemView.context

                ivGender.setColorFilter(
                    ContextCompat.getColor(context, R.color.color_soft_black),
                    PorterDuff.Mode.SRC_IN
                )

                handleShareProfileMessage(
                    message = currentMessage,
                    ivAvatar = ivAvatar,
                    tvName = tvName,
                    tvUniqueName = tvUniqueName,
                    flShareProfileInfo = llShareProfileInfo,
                    ivGender = ivGender,
                    tvAgeAddress = tvAgeAddress,
                    clickListener = onMessageClicked,
                    isSenderMessage = false
                )

                setMessageTime(tvChatTime, message.createdAt)
                handleMessageHeader(context, messageLayout, currentMessage)
                setMessageContainerTopMargin(
                    messageLayout,
                    currentMessage,
                    prevMessage
                )
                handleReplyMessage(currentMessage, onMessageClicked)
                highlightMessageScrollAnimation(
                    false, currentMessage, llMessageBubbleContainer
                )

                fwdContainer.setMargins(bottom = FWD_CONTAINER_MARGIN_BOTTOM_8.dp)
                handleMessageForwarding(currentMessage, fwdContainer, tvFwdAuthorName, onMessageClicked)
                // 30.08.2021 - поделиться профилем в групповой чат на данном этапе нельзя
            }
        }

        override fun getSwipeContainer() = llMessageBubbleContainer
    }

    inner class SenderShareCommunityViewHolder(view: View) : BaseMessageViewHolder(view), ISwipeableHolder {

        private val llMessageBubbleContainer: LinearLayout =
            view.findViewById(R.id.ll_message_bubble_container)
        private val messageLayout: ViewGroup = view.findViewById(R.id.layout_chat_message)

        private val ivAvatar: ImageView = view.findViewById(R.id.iv_share_community_avatar_send)
        private val tvName: TextView = view.findViewById(R.id.tv_share_community_name_send)
        private val tvDescription: TextView = view.findViewById(R.id.tv_share_community_description_send)
        private val tvChatTime: TextView = view.findViewById(R.id.tv_chat_time)
        private val fwdContainer: LinearLayout = view.findViewById(R.id.forward_container_send)
        private val tvFwdAuthorName: TextView = view.findViewById(R.id.tv_fwd_author_name)
        private val tvMessageEdited: TextView = view.findViewById(R.id.tv_message_edited)

        fun bind(message: MessageEntity?, prevMessage: MessageEntity?) {
            message?.let { currentMessage ->
                tvMessageEdited.isVisible = isMessageEditEnabled && currentMessage.isEdited()
                fwdContainer.setMargins(bottom = FWD_CONTAINER_MARGIN_BOTTOM_8.dp)
                val isForwarding =
                    handleMessageForwarding(currentMessage, fwdContainer, tvFwdAuthorName, onMessageClicked)

                handleShareCommunityMessage(
                    message = currentMessage,
                    ivAvatar = ivAvatar,
                    tvName = tvName,
                    tvDescription = tvDescription,
                    clickListener = onMessageClicked,
                    isSenderMessage = !isForwarding
                )

                setMessageTime(tvChatTime, message.createdAt)
                handleMessageHeader(itemView.context, messageLayout, currentMessage)
                setMessageContainerTopMargin(
                    messageLayout,
                    currentMessage,
                    prevMessage
                )
                handleReplyMessage(currentMessage, onMessageClicked)
                // 30.08.2021 - поделиться профилем в групповой чат на данном этапе нельзя
            }
        }

        override fun getSwipeContainer() = llMessageBubbleContainer

    }

    inner class ReceiverShareCommunityViewHolder(view: View) : BaseMessageViewHolder(view), ISwipeableHolder {

        private val llMessageBubbleContainer: FrameLayout =
            view.findViewById(R.id.whole_message_width_container)
        private val messageLayout: ViewGroup = view.findViewById(R.id.layout_chat_message)

        private val ivAvatar: ImageView = view.findViewById(R.id.iv_share_community_avatar_receive)
        private val tvName: TextView = view.findViewById(R.id.tv_share_community_name_receive)
        private val tvDescription: TextView = view.findViewById(R.id.tv_share_community_description_receive)
        private val tvChatTime: TextView = view.findViewById(R.id.tv_chat_time)
        private val fwdContainer: LinearLayout = view.findViewById(R.id.forward_container_receive)
        private val tvFwdAuthorName: TextView = view.findViewById(R.id.tv_fwd_author_name)

        fun bind(message: MessageEntity?, prevMessage: MessageEntity?) {
            message?.let { currentMessage ->
                handleShareCommunityMessage(
                    message = currentMessage,
                    ivAvatar = ivAvatar,
                    tvName = tvName,
                    tvDescription = tvDescription,
                    clickListener = onMessageClicked,
                    isSenderMessage = false
                )

                setMessageTime(tvChatTime, message.createdAt)
                handleMessageHeader(itemView.context, messageLayout, currentMessage)
                setMessageContainerTopMargin(
                    messageLayout,
                    currentMessage,
                    prevMessage
                )
                handleReplyMessage(currentMessage, onMessageClicked)
                highlightMessageScrollAnimation(
                    false, currentMessage, llMessageBubbleContainer
                )

                fwdContainer.setMargins(bottom = FWD_CONTAINER_MARGIN_BOTTOM_8.dp)
                handleMessageForwarding(currentMessage, fwdContainer, tvFwdAuthorName, onMessageClicked)
                // 30.08.2021 - поделиться профилем в групповой чат на данном этапе нельзя
            }
        }

        override fun getSwipeContainer() = llMessageBubbleContainer
    }


    inner class NoMediaViewHolder(view: View) : BaseMessageViewHolder(view) {

        private val messageLayout: ViewGroup = view.findViewById(R.id.layout_chat_message)
        private val tvChatTime: TextView = view.findViewById(R.id.tv_chat_time)

        fun bind(message: MessageEntity?, prevMessage: MessageEntity?) {
            message?.let { currentMessage ->
                setMessageTime(tvChatTime, message.createdAt)
                setMessageContainerTopMargin(
                    messageLayout,
                    currentMessage,
                    prevMessage
                )
            }
        }
    }

    inner class GreetingSendViewHolder(
        private val binding: ItemChatGreetingSendBinding,
        private val listener: IOnMessageClickedNew
    ) : BaseMessageViewHolder(binding.root), ISwipeableHolder {

        private var message: MessageEntity? = null

        override fun canSwipe() = isMessageSwipeEnabled(message)

        override fun getSwipeContainer() = binding.vgLayoutChatMessage

        fun bind(message: MessageEntity?, prevMessage: MessageEntity?) {
            message?.let { currentMessage ->
                itemView.tag = message.msgId
                val previouslyBoundMessage = this.message
                this.message = currentMessage

                setMessageTime(
                    tvTime = binding.tvChatTime,
                    createdAt = message.createdAt
                )

                initSticker(message.attachment, previouslyBoundMessage?.attachment)

                handleActionProgress(
                    ivError = binding.ivSenderSimpleImageMessageError,
                    ivSent = binding.ivMarkerSent,
                    bubbleContainer = binding.vgChatBubbleBackground,
                    tapContainer = binding.vgResendTapContainer,
                    message = message
                )
                handleMessageStatus(
                    ivDelivered = binding.ivMarkerDelivered,
                    ivRead = binding.ivMarkerRead,
                    ivSent = binding.ivMarkerSent,
                    isDelivered = message.delivered,
                    isRead = message.readed,
                    isSent = message.sent
                )
                setMessageContainerTopMargin(
                    messageLayout = binding.vgLayoutChatMessage,
                    currMessage = currentMessage,
                    prevMessage = prevMessage
                )
                highlightMessageScrollAnimation(
                    isSender = true,
                    message = currentMessage,
                    container = binding.root,
                )
                handleReplyMessage(
                    currentMessage = currentMessage,
                    onMessageClicked = listener
                )

                handleMessageForwarding(
                    message = currentMessage,
                    container = binding.forwardContainerSend.vgContainerSend,
                    tvName = binding.forwardContainerSend.tvFwdAuthorName,
                    onMessageClicked = listener
                )

                binding.vgChatBubbleBackground.setOnLongClickListener {
                    listener.onMessageLongClicked(message)
                    true
                }
            }
        }

        private fun initSticker(attachment: MessageAttachment, prevAttachment: MessageAttachment?) {
            val sameAttachment = prevAttachment == attachment
            val isStickerAlreadyShown = (binding.lavChatSticker.isAnimating ||
                binding.ivChatSticker.drawable != null) && sameAttachment
            if (!isStickerAlreadyShown) binding.vgStickerPlaceholder.visible()
            when {
                !attachment.lottieUrl.isNullOrBlank() && !isStickerAlreadyShown -> {
                    setupViewsForSticker()
                    binding.ivChatSticker.gone()
                    binding.lavChatSticker.visible()
                    binding.tvEmoji.gone()
                    binding.lavChatSticker.repeatCount = LottieDrawable.INFINITE
                    binding.lavChatSticker.setFailureListener { binding.vgStickerPlaceholder.visible() }
                    binding.lavChatSticker.addLottieOnCompositionLoadedListener { binding.vgStickerPlaceholder.gone() }
                    binding.lavChatSticker.setAnimationFromUrl(attachment.lottieUrl)
                    binding.lavChatSticker.resumeAnimation()
                }
                attachment.url.isNotBlank() && !isStickerAlreadyShown -> {
                    setupViewsForSticker()
                    binding.ivChatSticker.visible()
                    binding.lavChatSticker.gone()
                    binding.tvEmoji.gone()
                    binding.ivChatSticker.loadGlideGifWithCallback(attachment.url, onReady = {
                        binding.vgStickerPlaceholder.gone()
                    })
                }
                else -> {
                    setupViewsForEmoji()
                    binding.vgStickerPlaceholder.gone()
                    binding.ivChatSticker.gone()
                    binding.lavChatSticker.gone()
                    binding.tvEmoji.visible()
                }
            }
        }

        private fun setupViewsForSticker() {
            binding.forwardContainerSend.root.updatePadding(
                paddingStart = 0,
                paddingEnd = 0
            )
            binding.tvChatTime.updatePadding(
                paddingEnd = 0,
                paddingBottom = 0
            )
            binding.vgChatBubbleBackground.setBackgroundResource(0)
        }

        private fun setupViewsForEmoji() {
            binding.forwardContainerSend.root.updatePadding(
                paddingStart = 8.dp,
                paddingEnd = 8.dp
            )
            binding.tvChatTime.updatePadding(
                paddingEnd = 5.dp,
                paddingBottom = 6.dp
            )
            binding.vgChatBubbleBackground.setBackgroundResource(R.drawable.chat_message_shape_my)
        }
    }

    inner class GreetingReceiveViewHolder(
        private val binding: ItemChatGreetingReceiveBinding,
        private val listener: IOnMessageClickedNew
    ) : BaseMessageViewHolder(binding.root), ISwipeableHolder {

        private var message: MessageEntity? = null

        override fun canSwipe() = isMessageSwipeEnabled(message)

        override fun getSwipeContainer() = binding.vgLayoutChatMessage

        fun bind(message: MessageEntity?, prevMessage: MessageEntity?) {
            message?.let { currentMessage ->
                itemView.tag = message.msgId
                this.message = currentMessage

                setMessageTime(
                    tvTime = binding.tvChatTime,
                    createdAt = message.createdAt
                )

                initSticker(message.attachment)

                setMessageContainerTopMargin(
                    messageLayout = binding.vgLayoutChatMessage,
                    currMessage = currentMessage,
                    prevMessage = prevMessage
                )
                highlightMessageScrollAnimation(
                    isSender = false,
                    message = currentMessage,
                    container = binding.root,
                )
                handleReplyMessage(
                    currentMessage = currentMessage,
                    onMessageClicked = listener
                )

                handleMessageForwarding(
                    message = currentMessage,
                    container = binding.forwardContainerSend.vgContainerSend,
                    tvName = binding.forwardContainerSend.tvFwdAuthorName,
                    onMessageClicked = listener
                )

                binding.vgChatBubbleBackground.setOnLongClickListener {
                    listener.onMessageLongClicked(message)
                    true
                }
            }
        }

        private fun initSticker(attachment: MessageAttachment) {
            val isStickerAlreadyShown = binding.lavChatSticker.isAnimating || binding.ivChatSticker.drawable != null
            if (!isStickerAlreadyShown) binding.vgStickerPlaceholder.visible()
            when {
                !attachment.lottieUrl.isNullOrBlank() && !isStickerAlreadyShown -> {
                    setupViewsForSticker()
                    binding.ivChatSticker.gone()
                    binding.lavChatSticker.visible()
                    binding.tvEmoji.gone()
                    binding.lavChatSticker.repeatCount = LottieDrawable.INFINITE
                    binding.lavChatSticker.addLottieOnCompositionLoadedListener { binding.vgStickerPlaceholder.gone() }
                    binding.lavChatSticker.setFailureListener { binding.vgStickerPlaceholder.visible() }
                    binding.lavChatSticker.setAnimationFromUrl(attachment.lottieUrl)
                    binding.lavChatSticker.resumeAnimation()
                }
                attachment.url.isNotBlank() && !isStickerAlreadyShown -> {
                    setupViewsForSticker()
                    binding.ivChatSticker.visible()
                    binding.lavChatSticker.gone()
                    binding.tvEmoji.gone()
                    binding.ivChatSticker.loadGlideGifWithCallback(attachment.url, onReady = {
                        binding.vgStickerPlaceholder.gone()
                    })
                }
                !isStickerAlreadyShown -> {
                    setupViewsForEmoji()
                    binding.vgStickerPlaceholder.gone()
                    binding.ivChatSticker.gone()
                    binding.lavChatSticker.gone()
                    binding.tvEmoji.visible()
                }
            }
        }

        private fun setupViewsForSticker() {
            binding.forwardContainerSend.root.updatePadding(
                paddingStart = 0,
                paddingEnd = 0
            )
            binding.tvChatTime.updatePadding(
                paddingEnd = 0,
                paddingBottom = 0
            )
            binding.vgChatBubbleBackground.setBackgroundResource(0)
        }

        private fun setupViewsForEmoji() {
            binding.forwardContainerSend.root.updatePadding(
                paddingStart = 8.dp,
                paddingEnd = 8.dp
            )
            binding.tvChatTime.updatePadding(
                paddingEnd = 5.dp,
                paddingBottom = 6.dp
            )
            binding.vgChatBubbleBackground.setBackgroundResource(R.drawable.chat_message_shape_others)
        }
    }


    /**
     * Base view holder for all Messages
     */
    open inner class BaseMessageViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        protected var baseDividerContainer: ViewGroup? = null
        private val textContentMessageIdsSet = mutableSetOf<String>()
        protected var replyMessageHelper: ReplyMessageHelper? = null
        protected val rootReply: ViewGroup? = itemView.findViewById(R.id.reply_container)

        fun isMessageSwipeEnabled(message: MessageEntity?): Boolean {
            return message?.sent == true
                && room?.blocked == false
                && isRoomBlocked.not()
        }

        /**
         * Different sizes for emojis (Scale emoji depends on amount)
         */
        fun handleEmojis(
            textView: TextView,
            message: MessageEntity,
            needToZoomImoji: Boolean = true
        ) {
            val content = message.content.trim()
            val pair = EmojiUtils.stringEmojiData(content)
            val isHaveText = pair.first
            val countEmoji = pair.second

            if (!isHaveText && countEmoji > 0 && needToZoomImoji) {
                when (countEmoji) {
                    1 -> textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, EMOJI_SIZE_1_SP)
                    2 -> textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, EMOJI_SIZE_2_SP)
                    3 -> textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, EMOJI_SIZE_3_SP)
                }
                textView.setMargins(end = EMOJI_BUBBLE_MARGIN_RIGHT.dp)
                textView.text = content
            } else {
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, MESSAGE_TEXT_SIZE_SP)
                handleUniqueNames(message, textView)
            }

            textContentMessageIdsSet.add(message.msgId)
        }

        fun TextView.handlePostAuthorStatuses(post: Post?) {
            this.enableTopContentAuthorApprovedUser(
                params = TopAuthorApprovedUserModel(
                    approved = post?.user?.approved.toBoolean(),
                    customIconTopContent = R.drawable.ic_approved_author_gold_10,
                    isVip = post?.user?.accountType != AccountTypeEnum.ACCOUNT_TYPE_REGULAR.value,
                    interestingAuthor = post?.user?.topContentMaker.toBoolean(),
                    approvedIconSize = ApprovedIconSize.SMALL
                )
            )
        }

        private fun handleUniqueNames(message: MessageEntity, textView: TextView) {
            val is24HourFormat = DateFormat.is24HourFormat(itemView.context)

            val spaceMaskSend = when (is24HourFormat) {
                true -> TEXT_SPACE_DEFAULT_SEND_24H
                false -> TEXT_SPACE_DEFAULT_SEND_12H
            }
            val spaceMaskReceive = when (is24HourFormat) {
                true -> TEXT_SPACE_DEFAULT_RECEIVE_24H
                false -> TEXT_SPACE_DEFAULT_RECEIVE_12H
            }
            val birthdayTextColor = getBirthdayTextColor(message.creator?.userId)
            val tagColor: Int
            val spaceMask: String
            when (message.creator?.userId) {
                myUid -> {
                    tagColor = R.color.ui_color_chat_send_grey
                    spaceMask = spaceMaskSend
                }
                else -> {
                    tagColor = R.color.ui_purple
                    spaceMask = spaceMaskReceive
                }
            }

            if (message.tagSpan != null) {
                spanTagsChatText(
                    context = itemView.context,
                    tvText = textView,
                    post = message.tagSpan,
                    linkColor = tagColor,
                    spaceMask = spaceMask,
                    chatBirthdayData = ChatBirthdayUiEntity(
                        isSomeOneHasBirthday = isSomeBodyHasBirthday,
                        birthdayTextColor = birthdayTextColor,
                        birthdayTextRanges = message.birthdayRangesList
                    )
                ) { clickType ->
                    when (clickType) {
                        is SpanDataClickType.ClickUserId -> {
                            onMessageClicked.onUniquenameClicked(
                                clickType.userId ?: 0,
                                message = message
                            )
                        }
                        is SpanDataClickType.ClickUnknownUser -> {
                            onMessageClicked.onUniquenameUnknownProfileError()
                        }
                        is SpanDataClickType.ClickHashtag -> {
                            onMessageClicked.onHashtagClicked(clickType.hashtag)
                        }
                        is SpanDataClickType.ClickBirthdayText -> {
                            onMessageClicked.onBirthdayTextClicked()
                        }
                        is SpanDataClickType.ClickLink -> {
                            onMessageClicked.onLinkClicked(clickType.link)
                        }
                        else -> {
                            Timber.d("This data click type can not be handled.")
                        }
                    }
                }
            }
        }

        fun handleRepostTextUniqueNames(post: Post?, textView: TextView, linkColor: Int) {
            post?.text?.let {
                if (post.tagSpan != null) {
                    spanTagsChatText(
                        context = itemView.context,
                        tvText = textView,
                        post = post.tagSpan,
                        linkColor = linkColor,
                        isTrimText = true
                    )
                }
            }
        }

        fun handleRepostTitleUniqueNames(post: Post?, textView: TextView, linkColor: Int) {
            post?.event?.title?.let {
                if (post.event?.tagSpan != null) {
                    spanTagsChatText(
                        context = itemView.context,
                        tvText = textView,
                        post = post.event?.tagSpan,
                        linkColor = linkColor,
                        isTrimText = true
                    )
                }
            } ?: run {
                textView.text = ""
            }
        }

        fun handleReplyMessage(
            currentMessage: MessageEntity,
            onMessageClicked: IOnMessageClickedNew
        ) {
            replyMessageHelper = ReplyMessageHelper(rootReply)
            replyMessageHelper?.showReply(
                currentMessage.parentMessage?.toReplyEvent(),
                currentMessage.creator?.userId != myUid
            )
            rootReply?.click { onMessageClicked.onClickReplyParentMessage(currentMessage) }
        }

        /**
         * Метод измеряет 2 контейнера rootReply и bubble(сообщение)
         * и в зависимости от ширины (больше или меньше)
         * ставит на rootReply MATCH_PARENT или WRAP_CONTENT
         * для выравнивания плашки ответа и самого бабла сообщения
         *
         * !!! В данный момент метод нигде не вызывается (пусть будет, мож пригодится)
         */
        fun alignReplyMessageWidth(bubble: ViewGroup) {
            bubble.measure(WRAP_CONTENT, WRAP_CONTENT)
            val bubbleWidth = bubble.measuredWidth
            rootReply?.measure(WRAP_CONTENT, WRAP_CONTENT)
            val replyWidth = rootReply?.measuredWidth ?: 0
            if (bubbleWidth > replyWidth) {
                rootReply?.layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT)
                rootReply?.setMargins(start = 2.dp, top = 2.dp, end = 2.dp)
            } else {
                rootReply?.layoutParams = LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
                rootReply?.setMargins(start = 2.dp, top = 2.dp, end = 2.dp)
            }
        }

        fun setMessageTime(tvTime: TextView, createdAt: Long) {
            tvTime.text = getShortTime(
                millis = createdAt,
                is24hourMode = DateFormat.is24HourFormat(itemView.context.applicationContext)
            )
        }

        fun handleMessageStatus(
            ivDelivered: ImageView,
            ivRead: ImageView,
            ivSent: ImageView,
            isDelivered: Boolean,
            isRead: Boolean,
            isSent: Boolean
        ) {
            ivSent.isVisible = isSent && !(isDelivered || isRead)
            ivDelivered.isVisible = isDelivered && !isRead
            ivRead.isVisible = isRead
        }

        fun handleActionProgress(
            ivError: ImageView,
            ivSent: ImageView,
            bubbleContainer: ViewGroup,
            tapContainer: FrameLayout,
            message: MessageEntity,
            tvMessage: TextView? = null,
            pbProgress: ProgressBar? = null
        ) {
            handleLoadingProgress(
                bubbleContainer = bubbleContainer,
                isShowLoadingProgress = message.isShowLoadingProgress,
                pbProgress = pbProgress
            )

            handleResendProgress(
                ivError = ivError,
                ivSent = ivSent,
                bubbleContainer = bubbleContainer,
                tapContainer = tapContainer,
                message = message,
                tvMessage = tvMessage
            )
        }

        private fun handleLoadingProgress(
            bubbleContainer: ViewGroup,
            isShowLoadingProgress: Boolean,
            pbProgress: ProgressBar?
        ) {
            if (isShowLoadingProgress) {
                bubbleContainer.setMargins(end = BUBBLE_MARGIN_END_WHEN_ERROR.dp)
                pbProgress?.visible()
            } else {
                pbProgress?.gone()
            }
        }

        private fun handleResendProgress(
            ivError: ImageView,
            ivSent: ImageView,
            bubbleContainer: ViewGroup,
            tapContainer: FrameLayout,
            message: MessageEntity,
            tvMessage: TextView? = null
        ) {
            if (!message.sent) {
                bubbleContainer.setMargins(end = BUBBLE_MARGIN_END_WHEN_ERROR.dp)
                ivSent.gone()
                tapContainer.click {
                    onMessageClicked.onResendMessageClicked(message)
                }

                if (message.isResendProgress) {
                    ivError.setImageDrawable(
                        ContextCompat.getDrawable(
                            itemView.context,
                            R.drawable.ic_resend_message
                        )
                    )
                    ivError.visible()
                    ivError.startAnimation(
                        AnimationUtils
                            .loadAnimation(itemView.context, R.anim.rotate_indefinitely)
                    )
                } else {
                    ivError.clearAnimation()
                    ivError.setImageDrawable(
                        ContextCompat.getDrawable(
                            itemView.context,
                            R.drawable.ic_send_error
                        )
                    )
                    ivError.visible()
                }
                // Уменьшаем количество конечных пробелов в тексте для неотправленного сообщения
                tvMessage?.text?.let { text ->
                    if (text.length >= TEXT_TRIM_SPACE_SEND_COUNT) {
                        tvMessage.text = text.trimEnd()
                    }
                }
            } else if (message.isEditingProgress) {
                bubbleContainer.setMargins(end = BUBBLE_MARGIN_END_WHEN_ERROR.dp)
                ivError.visible()
                ivSent.gone()
                tapContainer.click {}
                ivError.setImageDrawable(ContextCompat.getDrawable(itemView.context, R.drawable.ic_resend_message))
                ivError.startAnimation(AnimationUtils.loadAnimation(itemView.context, R.anim.rotate_indefinitely))
                // Уменьшаем количество конечных пробелов в тексте для неотправленного сообщения
                tvMessage?.text?.let { text ->
                    if (text.length >= TEXT_TRIM_SPACE_SEND_COUNT) {
                        tvMessage.text = text.trimEnd()
                    }
                }
            } else {
                if (message.isShowLoadingProgress) return
                ivError.clearAnimation()
                ivError.gone()
                bubbleContainer.setMargins(end = 0.dp)
            }
        }

        fun changeDividerStyle() {
            if (isNewYearStyleEnabled) {
                baseDividerContainer?.children?.forEach { childView ->
                    when (childView) {
                        is TextView -> {
                            childView.setTextColor(Color.parseColor("#6A47D8"))
                        }
                        is ImageView -> {
                            val drawable = ContextCompat.getDrawable(
                                childView.context,
                                R.drawable.drawable_divider_decoration_new_year
                            )
                            childView.setImageDrawable(drawable)
                        }
                    }
                }
            } else {
                baseDividerContainer?.children?.forEach { childView ->
                    when (childView) {
                        is TextView -> {
                            childView.setTextColor(Color.parseColor("#7F7F7F"))
                        }
                        is ImageView -> {
                            val drawable = ContextCompat.getDrawable(
                                childView.context,
                                R.drawable.drawable_divider_decoration_gray
                            )
                            childView.setImageDrawable(drawable)
                        }
                    }
                }
            }
        }

        /** If current message is Receiver (i.e. our message) and previous one is Sender (i.e. not ours)
         * or vice-versa then increase the top margin
         */
         fun setMessageContainerTopMargin(
            messageLayout: ViewGroup,
            currMessage: MessageEntity,
            prevMessage: MessageEntity?
        ) {
            val topMarginDp = if (currMessage.creator?.userId != prevMessage?.creator?.userId) {
                MARGIN_TOP_OPPONENT_ITEMS
            } else {
                MARGIN_TOP_USER_ITEMS
            }
            messageLayout.setMargins(null, dpToPx(topMarginDp), null, null)
        }


        /**
         * Some content above message bubble
         */
        fun handleMessageHeader(
            context: Context,
            messageLayout: ViewGroup,
            currMessage: MessageEntity,
            dividerVisibility: (Boolean) -> Unit = {}
        ) {
            val container = messageLayout.findViewById<FrameLayout>(R.id.message_head_container)
            if (currMessage.isShowUnreadDivider || currMessage.msgId == messageWithUnreadDivider?.msgId) {
                val unreadDivider = View
                    .inflate(context, R.layout.chat_divider_unread_messages, null) as LinearLayout
                unreadDivider.layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, dpToPx(30))
                container.addView(unreadDivider)
                container.visible()
                dividerVisibility.invoke(true)
                messageWithUnreadDivider = currMessage
            } else {
                container.gone()
                dividerVisibility.invoke(false)
            }
        }

        fun trimPostLength(
            postTextView: TextViewWithImages,
            showMoreTextView: TextView,
            isImageExists: Boolean,
            postId: Long?
        ) {
            postTextView.doOnPreDraw {
                val maxLines = if (isImageExists) REPOST_TRIM_WITH_IMAGE else REPOST_TRIM_WITHOUT_IMAGE
                if (postTextView.lineCount > maxLines) {
                    postTextView.maxLines = maxLines
                    showMoreTextView.visible()
                    showMoreTextView.click {
                        postId?.let { onMessageClicked.onShowMoreRepost(it) }
                    }
                } else {
                    showMoreTextView.gone()
                }
            }
        }

        // ---------- Message scroll and color animation effects ------------------

        fun highlightMessageScrollAnimation(
            isSender: Boolean,
            message: MessageEntity,
            container: ViewGroup,
        ) {
            container.setBackgroundColor(Color.TRANSPARENT)
            val (messageId, highlightPosition) = highlightBackground
            if (messageId != message.msgId) return
            if (highlightPosition > 3) {
                container.setBackgroundResource(
                    if (isSender) R.drawable.animation_chat_scroll_sender
                    else R.drawable.animation_chat_scroll_receiver
                )
                val frameAnimation = container.background as AnimationDrawable
                frameAnimation.setEnterFadeDuration(ENTER_FADE_DURATION_MS)
                frameAnimation.setExitFadeDuration(EXIT_FADE_DURATION_MS)
                frameAnimation.start()
                itemView.doDelayed(DISABLE_SCROLL_DELAY_MS) {
                    container.setBackgroundColor(Color.TRANSPARENT)
                }
            }
            highlightBackground = Pair(null, RecyclerView.NO_POSITION)
        }

        fun setupVoiceMessage(
            currentMessage: MessageEntity,
            voiceView: VoiceMessageView
        ) {
            setFilePathToVoiceMessage(currentMessage, voiceView)
            voiceView.playButton.apply {
                click { onMessageClicked.onVoicePlayClicked(currentMessage, layoutPosition) }
                longClick { onMessageClicked.onVoiceMessageLongClicked(currentMessage) }
            }
            voiceView.visualizer.longClick {
                onMessageClicked.onVoiceMessageLongClicked(currentMessage)
            }
            voiceView.visualizer.setEventListener(object : DetectorSeekBar.IListener {
                override fun onClick(detectorSeekBar: DetectorSeekBar?) = Unit
                override fun onLongClick(detectorSeekBar: DetectorSeekBar?) {
                    onMessageClicked.onVoiceMessageLongClicked(message = currentMessage)
                }
            })
        }

        fun refreshVoiceMessage(
            message: MessageEntity?,
            voiceView: VoiceMessageView,
            isIncomingMessage: Boolean
        ) {
            message?.let { currentMessage ->
                setFilePathToVoiceMessage(currentMessage, voiceView)
                voiceView.setView(
                    isIncomingMessage = isIncomingMessage,
                    columnsHeightList = VisualizerVoiceView.getVoiceAmplitudes(currentMessage.attachment.waveForm)
                )
            }
        }

        private fun setFilePathToVoiceMessage(
            message: MessageEntity,
            voiceView: VoiceMessageView,
        ) {
            val fileUrl = message.attachment.url
            val fileName = Uri.parse(fileUrl).lastPathSegment
            val storageDir = File(
                act.getExternalFilesDir(null),
                "$CHAT_VOICE_MESSAGES_PATH/${message.roomId}"
            )
            val audioFile = File(storageDir, fileName)
            if (audioFile.exists()) {
                voiceView.downloadedFilePath = audioFile.absolutePath
            } else {
                voiceView.downloadedFilePath = null
            }
        }

        fun handleShareProfileMessage(
            message: MessageEntity,
            ivAvatar: ImageView,
            tvName: TextView,
            tvUniqueName: TextView,
            flShareProfileInfo: FrameLayout,
            ivGender: ImageView,
            tvAgeAddress: TextView,
            clickListener: IOnMessageClickedNew,
            isSenderMessage: Boolean = true
        ) {
            val userObj = gson.fromJson<UserSimple?>(message.attachment.metadata)
            userObj?.let { user ->
                val context = itemView.context
                val radius = BUBBLE_CORNER_RADIUS.dp.toFloat()
                val topLeftRadius = if (isSenderMessage) radius else 0f

                val avatar = if (user.profileDeleted == 1) R.drawable.chat_profile_deleted else {
                    if (user.avatarSmall.isNullOrEmpty()) R.drawable.fill_8 else user.avatarSmall
                }

                // Avatar
                ivAvatar.layout(0, 0, 0, 0) // Clear ImageView dim's before Glide fetch
                Glide.with(context)
                    .load(avatar)
                    .apply(
                        RequestOptions()
                            .transform(
                                CenterCrop(),
                                GranularRoundedCorners(topLeftRadius, 0f, 0f, radius)
                            )
                    )
                    .into(ivAvatar)

                tvName.text = user.name

                if (user.profileDeleted == 0) {
                    flShareProfileInfo.visible()
                    ivGender.visible()
                    tvAgeAddress.visible()
                    tvUniqueName.text = "$AT_SIGN${user.uniqueName}"

                    // Age address
                    val age: String
                    val gender: Int?
                    if (isHiddenAgeAndGender) {
                        age = String.empty()
                        gender = null
                    } else {
                        age = user.birthday?.let { "${getAge(it)}, " } ?: String.empty()
                        gender = user.gender
                    }
                    val city = user.city?.name ?: String.empty()
                    val country = user.country?.name ?: String.empty()

                    val genderSpaceMask = when (message.creator?.userId) {
                        myUid -> SHARE_PROFILE_GENDER_PADDING_START_SEND
                        else -> SHARE_PROFILE_GENDER_PADDING_START_RECEIVE
                    }

                    gender?.let { gender ->
                        tvAgeAddress.text = "$genderSpaceMask$age$city, $country"
                        ivGender.visible()
                        if (gender == USER_GENDER_MALE) {
                            ivGender.setImageDrawable(R.drawable.ic_sex_profile_info_male)
                        } else {
                            ivGender.setImageDrawable(R.drawable.ic_sex_profile_info_female)
                        }
                    } ?: let {
                        ivGender.gone()
                        tvAgeAddress.text = "$age$city, $country"
                    }

                } else {
                    tvUniqueName.text = context.getString(R.string.user_profile_unavailable)
                    ivGender.gone()
                    tvAgeAddress.gone()
                    flShareProfileInfo.gone()
                }

                itemView.click {
                    clickListener.onAvatarClicked(user.userId)
                }

                itemView.longClick {
                    onMessageClicked.onMessageLongClicked(message, itemView)
                }

            } ?: kotlin.run { Timber.e("User attachment parse ERROR") }
        }

        fun handleShareCommunityMessage(
            message: MessageEntity,
            ivAvatar: ImageView,
            tvName: TextView,
            tvDescription: TextView,
            clickListener: IOnMessageClickedNew,
            isSenderMessage: Boolean = true
        ) {
            val communityObj = gson.fromJson<CommunityShareEntity?>(message.attachment.metadata)
            communityObj?.let { group ->
                val context = itemView.context
                val radius = BUBBLE_CORNER_RADIUS.dp.toFloat()
                val topLeftRadius = if (isSenderMessage) radius else 0f

                val avatar = if (group.deleted == 1) R.drawable.chat_profile_deleted else {
                    if (group.avatar.isNullOrEmpty()) {
                        R.drawable.community_cover_image_placeholder_big
                    } else group.avatar
                }

                // Avatar
                ivAvatar.layout(0, 0, 0, 0) // Clear ImageView dim's before Glide fetch
                Glide.with(context)
                    .load(avatar)
                    .apply(
                        RequestOptions()
                            .transform(
                                CenterCrop(),
                                GranularRoundedCorners(topLeftRadius, 0f, 0f, radius)
                            )
                    )
                    .into(ivAvatar)

                tvName.text = group.name
                if (group.deleted == 0) {
                    tvDescription.text = if (group.private == 1)
                        context.getString(R.string.group_edit_fragment_close_option_name)
                    else context.getString(R.string.group_edit_fragment_open_option_name)
                } else {
                    tvDescription.text = context.getString(R.string.community_unavailable)
                }

                itemView.click {
                    clickListener.onCommunityClicked(
                        groupId = group.id,
                        isDeleted = group.deleted == 1
                    )
                }

                itemView.longClick {
                    onMessageClicked.onMessageLongClicked(message, itemView)
                }

            } ?: kotlin.run { Timber.e("COMMUNITY attachment parse ERROR") }
        }

        fun getGiftTypeId(map: HashMap<String, Any>?): Int? {
            return (map?.get(META_DATA_TYPE_ID) as? Double)?.toInt()
        }

        fun getGiftTitle(map: HashMap<String, Any>?): String? {
            return (map?.get(META_DATA_CUSTOM_TITLE) as? String)
        }

        fun handleMessageForwarding(
            message: MessageEntity,
            container: LinearLayout,
            tvName: TextView,
            onMessageClicked: IOnMessageClickedNew
        ): Boolean {
            return if (message.author != null) {
                container.visible()
                tvName.text = message.author?.name
                message.author?.userId?.let { id ->
                    container.click { onMessageClicked.onAvatarClicked(id) }
                }
                true
            } else {
                container.gone()
                false
            }
        }

        private fun getBirthdayTextColor(creatorUserId: Long?): Int {
            return if (creatorUserId == myUid) R.color.colorGoldHoliday else R.color.ui_purple
        }
    }

    companion object {
        const val EMOJI_SIZE_1_SP = 48.0f
        const val EMOJI_SIZE_2_SP = 34.0f
        const val EMOJI_SIZE_3_SP = 28.0f
        const val MESSAGE_TEXT_SIZE_SP = 18.0f
        const val MESSAGE_WIDTH_RELATIVE = 0.8f
        const val MIN_IMAGE_WIDTH = 122
        const val MIN_IMAGE_HEIGHT = 60
        const val MESSAGE_SIDE_MARGIN = 16
        const val MEDIA_RECYCLER_LOADING_PROGRESS_OFFSET = 20
        const val EMOJI_BUBBLE_MARGIN_RIGHT = 56

        // Количество символов, обрезаемое с конца не отправленного сообщения
        const val TEXT_TRIM_SPACE_SEND_COUNT = 4
        const val SHARE_PROFILE_GENDER_PADDING_START_SEND = "\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0\u00A0"
        const val SHARE_PROFILE_GENDER_PADDING_START_RECEIVE = "\u00A0\u00A0\u00A0\u00A0\u00A0"

        const val MARGIN_TOP_USER_ITEMS = 6 // dp
        const val MARGIN_TOP_OPPONENT_ITEMS = 12 // dp

        const val REPOST_TRIM_WITH_IMAGE = 3
        const val REPOST_TRIM_WITHOUT_IMAGE = 10

        const val BUBBLE_CORNER_RADIUS = 10

        const val BUBBLE_MARGIN_END_WHEN_ERROR = 26

        const val FWD_CONTAINER_MARGIN_BOTTOM_4 = 4
        const val FWD_CONTAINER_MARGIN_BOTTOM_10 = 10
        const val FWD_CONTAINER_MARGIN_BOTTOM_8 = 8

        val FWD_CONTAINER_VOICE_MSG_MARGIN = MarginData(
            start = 16.dp,
            top = 4.dp,
            end = 16.dp
        )

        const val BLUR_OVERLAY_CONTAINER_HEIGHT_MIN = 96
        const val DEFAULT_MEDIA_CONTAINER_HEIGHT = 320

        private const val PAYLOAD_VOICE_REBIND = "payload_voice_rebind"
        private const val PAYLOAD_VOICE_TEXT_NOW_RECOGNIZED = "payload_voice_text_now_recognized"
        private const val PAYLOAD_REFRESH_MESSAGE_ITEM = "payload_refresh_message_item"
        private const val PAYLOAD_ATTACHMENT_URL = "payload_attachment_url"


        private val diffCallback = object : DiffUtil.ItemCallback<MessageEntity>() {

            override fun areItemsTheSame(oldItem: MessageEntity, newItem: MessageEntity): Boolean =
                oldItem.msgId == newItem.msgId

            override fun areContentsTheSame(oldItem: MessageEntity, newItem: MessageEntity): Boolean =
                oldItem == newItem

            override fun getChangePayload(oldItem: MessageEntity, newItem: MessageEntity): Any? {
                val diff = Bundle()
                val oldAttach = oldItem.attachment
                val newAttach = newItem.attachment
                when {
                    oldItem.refreshMessageItem != newItem.refreshMessageItem -> {
                        diff.putString(PAYLOAD_REFRESH_MESSAGE_ITEM, "Refresh message item")
                    }
                    newItem.isEdited() -> {
                        diff.putString(PAYLOAD_REFRESH_MESSAGE_ITEM, "Refresh message item")
                    }
                    oldAttach.audioRecognizedText != newAttach.audioRecognizedText -> {
                        diff.putString(PAYLOAD_VOICE_TEXT_NOW_RECOGNIZED, newAttach.audioRecognizedText)
                    }
                    oldItem.isVoiceMessage() && newItem.isVoiceMessage()
                        && oldAttach.url == newAttach.url
                        && oldItem.readed == newItem.readed
                        && oldItem.delivered == newItem.delivered ->
                    {
                        diff.putString(PAYLOAD_VOICE_REBIND, "Voice URL:${newAttach.url}")
                    }
                }
                if (diff.size() == 0) return null
                return diff
            }

            private fun MessageEntity.isVoiceMessage() =
                itemType == ITEM_TYPE_AUDIO_SEND || itemType == ITEM_TYPE_AUDIO_RECEIVE

        }
    }



    data class MarginData(
        val start: Int = 0,
        val top: Int = 0,
        val end: Int = 0,
        val bottom: Int = 0
    )

    fun createSpanTextWithSpace(messageText: String, spaceMask: String): SpannableStringBuilder {
        val text = SpannableStringBuilder()
        text.append(messageText)
        text.append(spaceMask)
        text.setSpan(
            AbsoluteSizeSpan(MESSAGE_TEXT_SIZE_SP.toInt(), true),
            messageText.length, text.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        return text
    }
}
