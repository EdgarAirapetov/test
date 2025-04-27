package com.numplates.nomera3.presentation.view.adapter.newchat

import android.content.Context
import android.os.Build
import android.text.Html
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.meera.core.extensions.dp
import com.meera.core.extensions.empty
import com.meera.core.extensions.gone
import com.meera.core.extensions.inflate
import com.meera.core.extensions.setTextStyle
import com.meera.core.extensions.string
import com.meera.core.extensions.textColor
import com.meera.core.extensions.visible
import com.meera.core.utils.enableApprovedIcon
import com.meera.core.utils.getTimeForRooms
import com.meera.core.utils.isBirthdayToday
import com.meera.db.models.dialog.DialogEntity
import com.meera.db.models.dialog.LastMessage
import com.meera.db.models.dialog.UserChat
import com.numplates.nomera3.R
import com.numplates.nomera3.data.network.core.INetworkValues
import com.numplates.nomera3.data.newmessenger.CHAT_ITEM_TYPE_EVENT
import com.numplates.nomera3.data.newmessenger.ROOM_TYPE_DIALOG
import com.numplates.nomera3.data.newmessenger.ROOM_TYPE_GROUP
import com.numplates.nomera3.data.newmessenger.TYPING_TYPE_AUDIO
import com.numplates.nomera3.data.newmessenger.TYPING_TYPE_GIF
import com.numplates.nomera3.data.newmessenger.TYPING_TYPE_IMAGE
import com.numplates.nomera3.data.newmessenger.TYPING_TYPE_STICKER
import com.numplates.nomera3.data.newmessenger.TYPING_TYPE_VIDEO
import com.numplates.nomera3.modules.chat.IOnDialogClickedNew
import com.numplates.nomera3.modules.chat.helpers.isNotBlocked
import com.numplates.nomera3.modules.chat.messages.domain.model.AttachmentType
import com.numplates.nomera3.modules.featuretoggles.FeatureTogglesContainer
import com.numplates.nomera3.presentation.model.enums.CallStatusEnum
import com.numplates.nomera3.presentation.model.enums.ChatEventEnum
import java.util.Locale

private const val ICON_MIC_DRAWABLE_PADDING = 4

class ChatRoomsPagedAdapterV2(
    private val userId: Long,
    private val featureToggles: FeatureTogglesContainer,
    private val onDialogClicked: IOnDialogClickedNew
) : PagedListAdapter<DialogEntity, RecyclerView.ViewHolder>(diffCallback) {

    fun getItemAt(position: Int): DialogEntity? {
        return if (currentList?.isNotEmpty() == true) {
            currentList?.get(position)
        } else null
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        RoomsViewHolder(parent.inflate(R.layout.item_dialogs_room), userId, featureToggles, onDialogClicked)

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        (holder as RoomsViewHolder).bind(item)
    }

    /**
     * List rooms view holder
     */
    class RoomsViewHolder(
        itemView: View,
        private val userId: Long,
        private val featureToggles: FeatureTogglesContainer,
        private val onDialogClicked: IOnDialogClickedNew
    ) : RecyclerView.ViewHolder(itemView), View.OnClickListener, View.OnLongClickListener {

        private val tvRoomName: TextView = itemView.findViewById(R.id.tv_room_header)
        private val ivMute: ImageView = itemView.findViewById(R.id.iv_mute)
        private val cvIconBadge: CardView = itemView.findViewById(R.id.cv_icon_badge)
        private val ivIconGradientBg: ImageView = itemView.findViewById(R.id.iv_gradient_bg)
        private val ivIconImage: ImageView = itemView.findViewById(R.id.iv_icon_image)
        private val birthdayBackground: ImageView = itemView.findViewById(R.id.birthday_background)
        private val ivRoomAvatar: ImageView = itemView.findViewById(R.id.vipview_room)
        private val tvLastMessage: TextView = itemView.findViewById(R.id.tv_room_message)
        private val tvDraft: TextView = itemView.findViewById(R.id.tv_room_draft)
        private val tvTimeRoom: TextView = itemView.findViewById(R.id.tv_time_room)
        private val tvUnreadCount: TextView = itemView.findViewById(R.id.tv_unread_count)
        private val tvMentionsUnreadCount: TextView = itemView.findViewById(R.id.tv_mentions_unread_count)
        private val layoutRoom: ConstraintLayout = itemView.findViewById(R.id.room_container)
        private val statusError: ImageView? = itemView.findViewById(R.id.iv_undelivered_last_message)

        // Last message status
        private val ivStatusRead: ImageView = itemView.findViewById(R.id.iv_status_read_room)

        private var dialogItem: DialogEntity? = null

        init {
            layoutRoom.setOnClickListener(this)
            layoutRoom.setOnLongClickListener(this)
        }

        fun bind(room: DialogEntity?) {
            this.dialogItem = room
            tvRoomName.enableApprovedIcon(false)
            room?.let {
                // Title
                when (room.type) {
                    ROOM_TYPE_DIALOG -> {
                        cvIconBadge.gone()
                        setupDialogChatAvatar(room.companion)
                        tvRoomName.text = room.companion.name
                        tvRoomName.enableApprovedIcon(
                            enabled = room.companion.approved == 1,
                            isVip = room.companion.accountType != INetworkValues.ACCOUNT_TYPE_REGULAR
                        )
                        tvMentionsUnreadCount.gone()
                        val isBirthday = room.companion.birthDate
                            ?.let { isBirthdayToday(it) } ?: false
                            && (room.companion.blacklistedMe ?: 0) == 0
                        if (isBirthday) {
                            cvIconBadge.visible()
                            ivIconGradientBg.gone()
                            ivIconImage.setImageResource(R.drawable.ic_birthday_fg)
                            birthdayBackground.visible()
                        } else {
                            cvIconBadge.gone()
                            birthdayBackground.gone()
                        }
                    }
                    ROOM_TYPE_GROUP -> {
                        birthdayBackground.gone()
                        cvIconBadge.visible()
                        ivIconGradientBg.visible()
                        ivIconImage.setImageResource(R.drawable.ic_group_fg)
                        setupGroupChatAvatar(room)
                        tvRoomName.text = room.title
                        setupMentionsUnreadCounter(room) // Mentions badge (@) only in group chat
                    }
                }

                setupUnreadCounter(room)
                setupDraft(
                    room = room,
                    draftStatus = { isDraft ->
                        if (isDraft) {
                           restoreDraftMode()
                        } else {
                            showLastMessage(room)
                        }
                    }
                )
                showErrorStatus(room)
                goneMicIcon(room)
            }
        }


        private fun showLastMessage(room: DialogEntity) {
            val lastMessageText = getLastMessage(
                context = itemView.context,
                room = room,
                myUid = userId,
                onVoiceRecognized = { userName, isGroupChat ->
                    handleVoceRecognizedLastMessageOwnerName(room, userName, isGroupChat)
                }
            )
            setHtmlLastMessage(
                textView = tvLastMessage,
                text = lastMessageText
            )
        }

        // Show ERROR status (last message not sent)
        private fun showErrorStatus(room: DialogEntity) {
            if (room.lastMessage?.sent == false && room.needToShowUnreadBadge == true) {
                statusError?.visible()
                hideAllMessageStatuses()
            } else {
                statusError?.gone()
            }
        }

        private fun goneMicIcon(room: DialogEntity) {
            val lastMessageAttachment = room.lastMessage?.attachment
            if (lastMessageAttachment?.type != TYPING_TYPE_AUDIO || lastMessageAttachment.audioRecognizedText.isEmpty()
            ) {
                tvLastMessage.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
                tvLastMessage.compoundDrawablePadding = 0
            }
        }

        private fun restoreDraftMode() {
            tvDraft.setTextColor(ContextCompat.getColor(itemView.context, R.color.ui_red))
            tvDraft.text = itemView.context.getString(R.string.draft)
            tvLastMessage.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
            tvLastMessage.compoundDrawablePadding = 0
            tvDraft.visible()
        }

        private fun handleVoceRecognizedLastMessageOwnerName(
            room: DialogEntity,
            userName: String,
            isGroupChat: Boolean
        ) {
            val recognizedMessage = room.lastMessage?.attachment?.audioRecognizedText ?: String.empty()
            if (isGroupChat) {
                voceRecognizedOwnerNameForGroup(userName, recognizedMessage)
            } else {
                voceRecognizedOwnerNameForDialog(room, recognizedMessage)
            }

            handleRecognizedVoiceMessageMicIcon(recognizedMessage)
        }

        private fun handleRecognizedVoiceMessageMicIcon(recognizedMessage: String) {
            if (recognizedMessage.isNotEmpty()) {
                tvLastMessage.setCompoundDrawablesWithIntrinsicBounds(R.drawable.mic_room_item, 0, 0, 0)
                tvLastMessage.compoundDrawablePadding = ICON_MIC_DRAWABLE_PADDING.dp
            } else {
                tvLastMessage.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
                tvLastMessage.compoundDrawablePadding = 0
            }
        }

        private fun voceRecognizedOwnerNameForDialog(room: DialogEntity, recognizedMessage: String) {
            if (room.lastMessage?.creator?.userId == userId && recognizedMessage.isNotEmpty()){
                tvDraft.setTextStyle(R.style.Black85Regular16)
                tvDraft.text = "${itemView.context.getString(R.string.me)}:"
                tvDraft.visible()
            } else {
                tvDraft.gone()
            }
        }

        private fun voceRecognizedOwnerNameForGroup(userName: String, recognizedMessage: String) {
            val textView = if (recognizedMessage.isNotEmpty()) {
                tvDraft.visible()
                tvDraft
            } else {
                tvDraft.gone()
                tvLastMessage
            }
            setHtmlLastMessage(textView, userName)
        }

        private fun setupDraft(room: DialogEntity, draftStatus: (isDraft: Boolean) -> Unit) {
            val draft = room.draft
            when {
                draft == null -> {
                    tvDraft.gone()
                    tvLastMessage.visible()
                    tvLastMessage.text = String.empty()
                    tvLastMessage.textColor(R.color.black_85)
                    draftStatus(false)
                }
                !draft.text.isNullOrEmpty() && room.isNotBlocked() -> {
                    tvDraft.visible()
                    ivStatusRead.gone()
                    tvLastMessage.visible()
                    tvLastMessage.text = draft.text
                    tvLastMessage.textColor(R.color.ui_gray_80)
                    draftStatus(true)
                }
                draft.reply != null && room.isNotBlocked() -> {
                    tvDraft.visible()
                    ivStatusRead.gone()
                    tvLastMessage.gone()
                    draftStatus(true)
                }
                else -> {
                    tvDraft.gone()
                    tvLastMessage.visible()
                    tvLastMessage.text = String.empty()
                    tvLastMessage.textColor(R.color.black_85)
                    draftStatus(false)
                }
            }
        }

        private fun setupDialogChatAvatar(user: UserChat) {
            // Show user avatar
            Glide.with(itemView)
                .load(user.avatarSmall)
                .apply(
                    RequestOptions
                        .circleCropTransform()
                        .placeholder(R.drawable.fill_8_round)
                        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                )
                .transition(DrawableTransitionOptions.withCrossFade(200))
                .into(ivRoomAvatar)
        }

        private fun setupGroupChatAvatar(dialog: DialogEntity) {
            Glide.with(itemView)
                .load(dialog.groupAvatar)
                .apply(
                    RequestOptions
                        .circleCropTransform()
                        .placeholder(R.drawable.group_chat_avatar_circle)
                        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                )
                .transition(DrawableTransitionOptions.withCrossFade(200))
                .into(ivRoomAvatar)
        }

        private fun setHtmlLastMessage(textView: TextView, text: String) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                textView.text = Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY)
            } else {
                textView.text = Html.fromHtml(text)
            }
        }

        private fun getLastMessage(
            context: Context,
            room: DialogEntity,
            myUid: Long,
            onVoiceRecognized: (userName: String, isGroupChat: Boolean) -> Unit
        ): String {
            val tagFontStart = "<font color='#6a48d9'>"
            val tagFontEnd = "</font>"

            val lastMessage = room.lastMessage
            val attachmentType = lastMessage?.attachment?.type

            when (lastMessage?.eventCode) {
                ChatEventEnum.SHARE_PROFILE.state ->
                    return "$tagFontStart${context.getString(R.string.profile_info)}$tagFontEnd" addMessageOwner lastMessage
                ChatEventEnum.SHARE_COMMUNITY.state ->
                    return "$tagFontStart${context.getString(R.string.general_community)}$tagFontEnd" addMessageOwner lastMessage
                ChatEventEnum.GIFT.state ->
                    return "$tagFontStart${
                        context.getString(R.string.notification_gift)
                            .capitalize()
                    }$tagFontEnd" addMessageOwner lastMessage
            }
            if (lastMessage?.deleted == true) {
                return "${tagFontStart}${context.getString(R.string.chat_message_deleted)}$tagFontEnd"
            }
            val validAttachmentImage = attachmentType == TYPING_TYPE_IMAGE
                || attachmentType == TYPING_TYPE_GIF
            val isOneAttachment = (lastMessage?.attachments?.size ?: 0) <= 1
            if (lastMessage?.content?.isNotEmpty() == true && validAttachmentImage) {
                return if (isOneAttachment) {
                    val gifText = context.getString(R.string.chat_gif_message, String.empty())
                    val imageText = context.getString(R.string.chat_image_message, String.empty())
                    val readyText = if (attachmentType == TYPING_TYPE_IMAGE) imageText else gifText
                    "$tagFontStart${readyText}$tagFontEnd" addMessageOwner lastMessage
                } else {
                    "<font color='#6a48d9'>${context.getString(R.string.chat_many_images_message)}</font>" addMessageOwner lastMessage
                }
            }

            lastMessage?.attachments?.let { att ->
                if (att.size > 1) {
                    return "<font color='#6a48d9'>${context.getString(R.string.chat_many_images_message)}</font>" addMessageOwner lastMessage
                }
            }

            if (lastMessage?.content?.isNotEmpty() == true) {
                // Blue color all event messages
                if (lastMessage.type == CHAT_ITEM_TYPE_EVENT) {
                    return "$tagFontStart${lastMessage.content}$tagFontEnd"
                }

                // Blue color user name in group chat
                if (room.type == ROOM_TYPE_GROUP) {
                    return if (room.lastMessage?.creator?.userId == myUid) {
                        val userName = context.getString(R.string.me)
                        "$userName: ${lastMessage.content}"
                    } else {
                        val userName = room.lastMessage?.creator?.name
                        "$tagFontStart<b>$userName:</b>$tagFontEnd ${lastMessage.content}"
                    }
                }

                // Deleted message
                if (lastMessage.deleted) {
                    return "${tagFontStart}${context.getString(R.string.chat_message_deleted)}$tagFontEnd"
                }

                //Black color for simple message
                return handleSimpleMessages(context, myUid, lastMessage, room, tagFontStart, tagFontEnd)
            } else {
                // Message text is EMPTY

                // Deleted message
                if (lastMessage?.deleted == true) {
                    return "${tagFontStart}${context.getString(R.string.chat_message_deleted)}$tagFontEnd"
                }

                // Repost
                if (lastMessage?.eventCode == ChatEventEnum.REPOST.state) {
                    return if (lastMessage.creator.userId != myUid) {
                        context.getString(getUserRepostStringResId(lastMessage)) addMessageOwner lastMessage
                    } else {
                        val repostMessage = context.getString(getMyRepostStringResId(lastMessage))
                        "$tagFontStart${repostMessage}$tagFontEnd" addMessageOwner lastMessage
                    }
                }

                // Repost moment
                if (lastMessage?.eventCode == ChatEventEnum.MOMENT.state) {
                    return if (lastMessage?.creator?.userId != myUid) {
                        context.getString(R.string.send_you_a_moment) addMessageOwner lastMessage
                    } else {
                        "$tagFontStart${context.getString(R.string.chat_repost_moment_message)}$tagFontEnd" addMessageOwner lastMessage
                    }
                }

                var userName = String.empty()

                // Group chat
                if (room.type == ROOM_TYPE_GROUP) {
                    room.lastMessage?.creator?.name?.let { userName = "$tagFontStart<b>$it:</b>$tagFontEnd" }
                    return when (attachmentType) {
                        TYPING_TYPE_IMAGE -> context.getString(R.string.chat_image_message, userName)
                        TYPING_TYPE_GIF -> context.getString(R.string.chat_gif_message, userName)
                        TYPING_TYPE_AUDIO -> lastVoiceMessage(lastMessage, userName, true, onVoiceRecognized)
                        TYPING_TYPE_VIDEO -> "$userName ${context.getString(R.string.video)}"
                        TYPING_TYPE_STICKER -> "$userName ${context.getString(R.string.sticker)}"
                        else -> String.empty()
                    }
                }

                if (lastMessage?.attachment?.url?.endsWith(".gif") == true) {
                    return "$tagFontStart${
                        context.getString(
                            R.string.chat_gif_message,
                            userName
                        )
                    }$tagFontEnd" addMessageOwner lastMessage
                }

                return when (attachmentType) {
                    TYPING_TYPE_IMAGE -> "$tagFontStart${
                        context.getString(
                            R.string.chat_image_message,
                            userName
                        )
                    }$tagFontEnd" addMessageOwner lastMessage
                    TYPING_TYPE_GIF -> "$tagFontStart${
                        context.getString(
                            R.string.chat_gif_message,
                            userName
                        )
                    }$tagFontEnd" addMessageOwner lastMessage
                    TYPING_TYPE_AUDIO -> lastVoiceMessage(lastMessage, userName, false, onVoiceRecognized)
                    TYPING_TYPE_VIDEO -> "$tagFontStart ${context.getString(R.string.video)} $tagFontEnd" addMessageOwner lastMessage
                    TYPING_TYPE_STICKER -> "$tagFontStart ${context.getString(R.string.sticker)} $tagFontEnd" addMessageOwner lastMessage
                    else -> String.empty()
                }
            }
        }

        private fun getUserRepostStringResId(message: LastMessage) =
            if (message.attachment?.type == AttachmentType.EVENT.type) {
                R.string.send_you_an_event_post
            } else {
                R.string.send_you_a_post
            }

        private fun getMyRepostStringResId(message: LastMessage) =
            if (message.attachment?.type == AttachmentType.EVENT.type) {
                R.string.chat_event_repost_message
            } else {
                R.string.chat_repost_message
            }

        private fun lastVoiceMessage(
            lastMessage: LastMessage,
            userName: String,
            isGroupChat: Boolean,
            onVoiceRecognized: (userName: String, isGroupChat: Boolean) -> Unit
        ): String {
            val isFeatureEnabled = featureToggles.chatLastMessageRecognizedText.isEnabled
            return if (isFeatureEnabled) {
                onVoiceRecognized.invoke(userName, isGroupChat)
                lastVoiceMessageRecognizedFeatureOn(lastMessage, userName, isGroupChat)
            } else {
                lastVoiceMessageRecognizedFeatureOff(lastMessage, userName)
            }
        }

        private fun lastVoiceMessageRecognizedFeatureOff(
            lastMessage: LastMessage,
            userName: String,
        ): String {
            return itemView.context
                .getString(R.string.chat_voice_message, userName) addMessageOwner lastMessage
        }

        private fun lastVoiceMessageRecognizedFeatureOn(
            lastMessage: LastMessage,
            userName: String,
            isGroupChat: Boolean
        ): String {
            val recognizedText = lastMessage.attachment?.audioRecognizedText
            return if (recognizedText?.isNotEmpty() == true) {
                "«${recognizedText}»"
            } else {
                val lastVoiceMessageText = itemView.context.getString(R.string.chat_voice_message, userName)
                if (isGroupChat) lastVoiceMessageText else lastVoiceMessageText addMessageOwner lastMessage
            }
        }

        /**
         * Handle simple messages (calls e.t.c)
         */
        private fun handleSimpleMessages(
            context: Context,
            myUid: Long,
            message: LastMessage,
            room: DialogEntity,
            tagFontStart: String,
            tagFontEnd: String
        ): String {
            when (message.eventCode) {
                // CALLs
                ChatEventEnum.CALL.state -> {
                    var incomingStr = context.getString(R.string.call_status_incoming_room)
                    var outgoingStr = context.getString(R.string.call_status_outgoing)
                    var cancelledStr = context.getString(R.string.canceled_call)
                    var missedStr = context.getString(R.string.call_status_missed)

                    if (room.type == ROOM_TYPE_GROUP) {
                        incomingStr = incomingStr.firstWord()
                        outgoingStr = outgoingStr.firstWord()
                        cancelledStr = cancelledStr.firstWord()
                        missedStr = missedStr.firstWord()
                    }
                    val isIncoming = message.metadata?.caller?.callerId != myUid

                    return if (isIncoming) {
                        when (message.metadata?.status) {
                            CallStatusEnum.DECLINED.status,
                            CallStatusEnum.REJECTED.status -> "$tagFontStart$missedStr$tagFontEnd"
                            else -> "$tagFontStart$incomingStr$tagFontEnd"
                        }
                    } else {
                        if (message.metadata?.status == CallStatusEnum.DECLINED.status) {
                            "$tagFontStart$cancelledStr$tagFontEnd"
                        } else {
                            "$tagFontStart$outgoingStr$tagFontEnd"
                        }
                    }
                }

                ChatEventEnum.REPOST.state ->
                    return if (message.creator.userId != myUid) {
                        context.getString(getUserRepostStringResId(message))
                    } else {
                        val repostMessage = context.getString(getMyRepostStringResId(message))
                        "$tagFontStart${repostMessage}$tagFontEnd" addMessageOwner message
                    }

                ChatEventEnum.GIFT.state ->
                    return "$tagFontStart${context.string(R.string.notification_gift)
                        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }}$tagFontEnd"
            }

            // By default only simple message
            return message.content.toString() addMessageOwner message
        }

        private fun String.firstWord(): String = this.substring(0, this.indexOf(" "))

        /**
         * If its your last message add "Your: message-text"
         */
        private infix fun String.addMessageOwner(message: LastMessage): String =
            if (message.creator.userId == userId)
                itemView.context.getString(R.string.me) + ": $this"
            else this

        private fun setupUnreadCounter(dialog: DialogEntity) {
            tvTimeRoom.text =  getTimeForRooms(
                context = itemView.context,
                timeMillis = dialog.lastMessageUpdatedAt
            )

            val isMuted = isMuted(dialog)
            val unreadMessage = dialog.unreadMessageCount
            if (unreadMessage != null) {
                if (unreadMessage > 0) {
                    hideAllMessageStatuses()
                    tvUnreadCount.text = unreadMessage.toString()
                    tvUnreadCount.visible()

                    setUpUnreadCounterColor(tvUnreadCount, isMuted)

                    handleCountSize(unreadMessage)
                } else {
                    tvUnreadCount.gone()
                    showLastMessageStatuses(dialog)
                }
            }

            setupMutedDrawable(isMuted)
        }

        private fun isMuted(dialog: DialogEntity): Boolean {
            return when {
                dialog.companion.settingsFlags?.notificationsOff == 1
                    && dialog.type == ROOM_TYPE_DIALOG -> true
                dialog.isMuted == true && dialog.type == ROOM_TYPE_GROUP -> true
                else -> false
            }
        }

        private fun setupMentionsUnreadCounter(dialog: DialogEntity) {
            dialog.unreadMentionsCount?.let { count ->
                if (count > 0) {
                    setUpUnreadCounterColor(tvMentionsUnreadCount, isMuted(dialog))
                    tvMentionsUnreadCount.visible()
                } else {
                    tvMentionsUnreadCount.gone()
                }
            } ?: let { tvMentionsUnreadCount.gone() }
        }

        private fun handleCountSize(count: Int) {
            val params = tvUnreadCount.layoutParams
            params?.width = if (count > 9) 30.dp else 20.dp
            tvUnreadCount.layoutParams = params
        }

        /**
         * Если для комнаты установлен режим без звука то данный метод размещает рядом с именем иконку
         * */
        private fun setupMutedDrawable(isMuted: Boolean) {
            if (isMuted) {
                ivMute.visible()
            } else {
                ivMute.gone()
            }
        }

        /**
         * Если для комнаты установлен режим без звука то данный метод помечает кружок серым цветом
         * */
        private fun setUpUnreadCounterColor(textView: TextView, isMuted: Boolean) {
            textView.background = if (isMuted)
                ContextCompat.getDrawable(itemView.context, R.drawable.circle_gray_bg_oval)
            else
                ContextCompat.getDrawable(itemView.context, R.drawable.circle_tab_bg)
        }

        private fun showLastMessageStatuses(dialog: DialogEntity) {
            ivStatusRead.visible()
            if (dialog.lastMessage?.readed == true) {
                ivStatusRead.setImageResource(R.drawable.ic_read_rooms_list)
            } else {
                if (dialog.lastMessage?.delivered == false) {
                    ivStatusRead.setImageResource(R.drawable.ic_sent_message_room)
                } else ivStatusRead.setImageResource(R.drawable.ic_delivered_rooms_list)
            }

            if (dialog.lastMessage?.creator?.userId != userId) hideAllMessageStatuses()
        }

        private fun hideAllMessageStatuses() {
            ivStatusRead.gone()
        }

        override fun onClick(view: View?) {
            when (view?.id) {
                R.id.room_container -> onDialogClicked.onRoomClicked(dialogItem)
                R.id.vipview_room -> onDialogClicked.onAvatarClicked(dialogItem)
            }
        }

        override fun onLongClick(p0: View?): Boolean {
            onDialogClicked.onRoomLongClicked(dialogItem)
            return true
        }
    }

    companion object {

        private val diffCallback = object : DiffUtil.ItemCallback<DialogEntity>() {
            override fun areItemsTheSame(oldItem: DialogEntity, newItem: DialogEntity): Boolean =
                oldItem.roomId == newItem.roomId

            override fun areContentsTheSame(oldItem: DialogEntity, newItem: DialogEntity): Boolean =
                oldItem.deleted == newItem.deleted
                    && oldItem.unreadMessageCount == newItem.unreadMessageCount
                    && oldItem.updatedAt == newItem.updatedAt
                    && oldItem.title == newItem.title
                    && oldItem.groupAvatar == newItem.groupAvatar
                    && oldItem.description == newItem.description
                    && oldItem.lastMessage == newItem.lastMessage
                    && oldItem.companion == newItem.companion
                    && oldItem.isMuted == newItem.isMuted
                    && oldItem.needToShowUnreadBadge == newItem.needToShowUnreadBadge
                    && oldItem.draft == newItem.draft
        }
    }
}
