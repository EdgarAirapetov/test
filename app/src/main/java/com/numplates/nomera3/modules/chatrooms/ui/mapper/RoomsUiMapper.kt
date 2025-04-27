package com.numplates.nomera3.modules.chatrooms.ui.mapper

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ImageSpan
import android.widget.ImageView
import androidx.core.text.color
import androidx.core.text.inSpans
import com.meera.core.extensions.asPrettyCount
import com.meera.core.extensions.empty
import com.meera.core.extensions.toBoolean
import com.meera.core.utils.getTimeForRooms
import com.meera.core.utils.isBirthdayToday
import com.meera.db.models.dialog.DialogEntity
import com.meera.db.models.dialog.LastMessage
import com.meera.uikit.widgets.chat.MessengerMedia
import com.meera.uikit.widgets.dpToPx
import com.meera.uikit.widgets.roomcell.SendStatus
import com.meera.uikit.widgets.roomcell.UiKitRoomCellConfig
import com.meera.uikit.widgets.userpic.UserpicStoriesStateEnum
import com.meera.uikit.widgets.userpic.UserpicUiModel
import com.numplates.nomera3.R
import com.numplates.nomera3.data.network.core.INetworkValues
import com.numplates.nomera3.data.newmessenger.CHAT_ITEM_TYPE_EVENT
import com.numplates.nomera3.data.newmessenger.ROOM_TYPE_DIALOG
import com.numplates.nomera3.data.newmessenger.ROOM_TYPE_GROUP
import com.numplates.nomera3.domain.interactornew.GetUserUidUseCase
import com.numplates.nomera3.modules.chat.messages.domain.model.AttachmentType
import com.numplates.nomera3.modules.featuretoggles.FeatureTogglesContainer
import com.numplates.nomera3.presentation.model.enums.CallStatusEnum
import com.numplates.nomera3.presentation.model.enums.ChatEventEnum
import javax.inject.Inject
import kotlin.math.min

private const val ICON_SPACING = 4
private const val DOUBLE_DOTS = ":"
private const val SPACE_SYMBOL = " "
private const val MAX_CHARS = 2

class RoomsUiMapper @Inject constructor(
    private val context: Context,
    private val getUserUidUseCase: GetUserUidUseCase,
    private val featureToggles: FeatureTogglesContainer
) {

    fun mapUiRoom(room: DialogEntity): UiKitRoomCellConfig {
        return UiKitRoomCellConfig(
            id = room.roomId,
            title = getTitle(room),
            content = getContent(room),
            userpicUiModel = getUserPictureConfig(room),
            isVerified = isVerified(room),
            isFlame = isTopContentMaker(room),
            isMuted = isMuted(room),
            dateTime = getTimeForRooms(context, room.lastMessageUpdatedAt),
            unreadMessages = getUnreadMessageCountText(room),
            isMentioned = isMentioned(room),
            checkedMode = false,
            isChecked = false,
            role = getUserRole(room),
            sendStatus = getSendStatus(room),
            isGroup = isGroupChat(room),
            isSentByMe = getSentByMe(room),
            backgroundMedia = isCompanionBirthday(room),
        )
    }

    private fun isCompanionBirthday(room: DialogEntity): MessengerMedia? {
        val isBirthday = isBirthdayToday(room.companion.birthDate) && !room.companion.blacklistedMe.toBoolean()
        return if (isBirthday) {
            MessengerMedia.Drawable(R.drawable.meera_img_room_cell_birthday)
        } else {
            null
        }
    }

    private fun getUnreadMessageCountText(room: DialogEntity): String? {
        val unreadCount = room.unreadMessageCount ?: 0
        return if (unreadCount > 0) unreadCount.asPrettyCount() else null
    }

    private fun getUserRole(room: DialogEntity): CharSequence? {
        return room.companion.role
    }

    private fun getSendStatus(room: DialogEntity): SendStatus {
        val unreadMessageCount = room.unreadMessageCount ?: 0
        return when {
            unreadMessageCount > 0 -> SendStatus.UNKNOWN
            room.lastMessage?.creator?.userId != getMyUid() -> SendStatus.UNKNOWN
            room.lastMessage?.readed == true -> SendStatus.READ
            room.lastMessage?.delivered == true -> SendStatus.DELIVERED
            room.lastMessage?.delivered == false -> SendStatus.SENT
            else -> SendStatus.UNKNOWN
        }
    }

    private fun isTopContentMaker(room: DialogEntity): Boolean {
        return if (!isVerified(room)) room.companion.topContentMaker.toBoolean() else false
    }

    private fun getSentByMe(room: DialogEntity): Boolean {
        return room.lastMessage?.creator?.userId == getMyUid()
    }

    private fun isGroupChat(room: DialogEntity): Boolean {
        return room.type == ROOM_TYPE_GROUP
    }

    private fun getUserPictureConfig(room: DialogEntity): UserpicUiModel {
        val user = room.companion
        val roomAvatarUrl = if (room.type == ROOM_TYPE_DIALOG) user.avatarSmall else room.groupAvatar
        val items = getTitle(room)
            .filter { it.isLetterOrDigit() || it.isWhitespace() }
            .uppercase()
            .trim()
            .replace("\\s+".toRegex(), " ")
            .split(SPACE_SYMBOL)

        val symbols = when {
            items.size >= MAX_CHARS -> "${items[0][0]}${items[1][0]}"
            items.isNotEmpty() -> items[0].substring(0, min(items[0].length, MAX_CHARS))
            else -> null
        }

        var userPic = UserpicUiModel(
            hat = isShowHat(room),
            userAvatarShow = !roomAvatarUrl.isNullOrBlank(),
            userAvatarUrl = roomAvatarUrl,
            userName = symbols,
            storiesState = storiesState(room),
            scaleType = ImageView.ScaleType.CENTER_CROP
        )

        if (room.type == ROOM_TYPE_GROUP && room.groupAvatar == null) {
            userPic = userPic.copy(
                userAvatarShow = true,
                userAvatarRes = R.drawable.meera_group_chat_avatar_placeholder
            )
        }
        return userPic
    }

    private fun storiesState(room: DialogEntity): UserpicStoriesStateEnum {
        val moments = room.companion.moments
        if (moments?.hasNewMoments.toBoolean()) return UserpicStoriesStateEnum.NEW
        if (moments?.hasMoments.toBoolean()) return UserpicStoriesStateEnum.VIEWED
        return UserpicStoriesStateEnum.NO_STORIES
    }

    private fun isShowHat(room: DialogEntity): Boolean {
        return room.companion.accountType == INetworkValues.ACCOUNT_TYPE_VIP
    }

    private fun getTitle(room: DialogEntity): String {
        return when (room.type) {
            ROOM_TYPE_DIALOG -> room.companion.name.orEmpty()
            ROOM_TYPE_GROUP -> room.title.orEmpty()
            else -> String.empty()
        }
    }

    private fun isVerified(room: DialogEntity): Boolean {
        return room.companion.approved.toBoolean()
    }

    private fun isMuted(room: DialogEntity): Boolean {
        return when {
            room.type == ROOM_TYPE_DIALOG && room.companion.settingsFlags?.notificationsOff == 1 -> true
            room.type == ROOM_TYPE_GROUP && room.isMuted == true -> true
            else -> false
        }
    }

    private fun isMentioned(room: DialogEntity): Boolean {
        val mentionsCount = room.unreadMentionsCount ?: 0
        return mentionsCount > 0
    }

    private fun getContent(room: DialogEntity): Spannable {
        val lastMessage = room.lastMessage

        if (room.draft != null) return composeCellContent(draft = room.draft?.text.orEmpty())
        if (lastMessage?.deleted == true) return SpannableStringBuilder(context.getString(R.string.chat_message_deleted))
        if (lastMessage?.type == CHAT_ITEM_TYPE_EVENT) return SpannableStringBuilder(lastMessage.content.orEmpty())

        val messageOwner = getLastMessageOwner(room)
        val content = when (lastMessage?.eventCode) {
            ChatEventEnum.TEXT.state -> composeCellContent(
                author = messageOwner,
                content = lastMessage.content
            )

            ChatEventEnum.IMAGE.state -> composeCellContent(
                author = messageOwner,
                content = context.getString(R.string.rooms_image_message)
            )

            ChatEventEnum.LIST.state -> composeCellContent(content = context.getString(R.string.rooms_many_images_message))
            ChatEventEnum.GIF.state -> composeCellContent(
                author = messageOwner,
                content = context.getString(R.string.rooms_gif_message)
            )

            ChatEventEnum.VIDEO.state -> composeCellContent(
                author = messageOwner,
                content = context.getString(R.string.rooms_video)
            )

            ChatEventEnum.AUDIO.state -> recognizedVoiceLastMessageContent(room, messageOwner)
            ChatEventEnum.GIFT.state -> composeCellContent(content = context.getString(R.string.rooms_gift))
            ChatEventEnum.SHARE_PROFILE.state -> composeCellContent(
                author = messageOwner,
                content = context.getString(R.string.rooms_profile_info)
            )

            ChatEventEnum.SHARE_COMMUNITY.state -> composeCellContent(
                author = messageOwner,
                content = context.getString(R.string.rooms_community)
            )

            ChatEventEnum.REPOST.state -> composeCellContent(
                author = messageOwner,
                content = repostLastMessageContent(room)
            )

            ChatEventEnum.MOMENT.state -> composeCellContent(
                author = messageOwner,
                content = repostMomentLastMessageContent(room)
            )

            ChatEventEnum.STICKER.state,
            ChatEventEnum.GREETING.state -> composeCellContent(
                author = messageOwner,
                content = context.getString(R.string.rooms_sticker)
            )

            ChatEventEnum.CALL.state -> composeCellContent(content = callLastMessageContent(room))
            else -> String.empty()
        }
        return SpannableStringBuilder(content)
    }

    private fun repostLastMessageContent(room: DialogEntity): String {
        val lastMessage = room.lastMessage
        val creatorUid = room.lastMessage?.creator?.userId ?: 0L
        return if (isMyLastMessage(creatorUid)) {
            context.getString(getMyRepostStringResId(lastMessage))
        } else {
            context.getString(getUserRepostStringResId(lastMessage))
        }
    }

    private fun repostMomentLastMessageContent(room: DialogEntity): String {
        val creatorUid = room.lastMessage?.creator?.userId ?: 0L
        return if (isMyLastMessage(creatorUid)) {
            context.getString(R.string.rooms_repost_moment_message)
        } else {
            context.getString(R.string.rooms_send_you_a_moment)
        }
    }

    private fun recognizedVoiceLastMessageContent(
        room: DialogEntity,
        messageOwner: String,
    ): Spannable {
        val lastMessage = room.lastMessage
        val isGroupChat = room.type == ROOM_TYPE_GROUP
        val isFeatureEnabled = featureToggles.chatLastMessageRecognizedText.isEnabled
        return if (isFeatureEnabled && !lastMessage?.attachment?.audioRecognizedText.isNullOrBlank()) {
            composeCellContent(
                author = messageOwner,
                recognizedAudio = lastVoiceMessageRecognizedFeatureOn(
                    lastMessage,
                    messageOwner,
                    isGroupChat
                )
            )
        } else {
            SpannableStringBuilder(context.getString(R.string.rooms_voice_message))
        }
    }

    private fun lastVoiceMessageRecognizedFeatureOn(
        lastMessage: LastMessage?,
        creatorName: String,
        isGroupChat: Boolean
    ): String {
        val recognizedText = lastMessage?.attachment?.audioRecognizedText
        return if (recognizedText?.isNotEmpty() == true) {
            "«${recognizedText}»"
        } else {
            val lastVoiceMessageText = context.getString(R.string.chat_voice_message, creatorName)
            if (isGroupChat) lastVoiceMessageText else "$creatorName: $lastVoiceMessageText"
        }
    }

    private fun callLastMessageContent(room: DialogEntity): String {
        val message = room.lastMessage

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
        val callerId = message?.metadata?.caller?.callerId ?: 0
        val isOutgoing = isMyLastMessage(callerId)

        return if (isOutgoing) {
            lastMessageTextForOutgoingMessage(message, cancelledStr, outgoingStr)
        } else {
            lastMessageTextForIncomingMessage(message, missedStr, incomingStr)
        }
    }

    private fun lastMessageTextForOutgoingMessage(
        message: LastMessage?,
        missedText: String,
        incomingText: String
    ): String {
        return when (message?.metadata?.status) {
            CallStatusEnum.DECLINED.status,
            CallStatusEnum.REJECTED.status -> missedText

            else -> incomingText
        }
    }

    private fun lastMessageTextForIncomingMessage(
        message: LastMessage?,
        cancelledText: String,
        outgoingText: String
    ): String {
        return if (message?.metadata?.status == CallStatusEnum.DECLINED.status) {
            cancelledText
        } else {
            outgoingText
        }
    }

    private fun String.firstWord(): String {
        return this.substring(0, this.indexOf(" "))
    }

    private fun getUserRepostStringResId(message: LastMessage?): Int {
        return if (message?.attachment?.type == AttachmentType.EVENT.type) {
            R.string.rooms_send_you_an_event_post
        } else {
            R.string.rooms_send_you_a_post
        }
    }

    private fun getMyRepostStringResId(message: LastMessage?): Int {
        return if (message?.attachment?.type == AttachmentType.EVENT.type) {
            R.string.rooms_event_repost_message
        } else {
            R.string.rooms_repost_message
        }
    }

    private fun getLastMessageOwner(room: DialogEntity): String {
        val creatorUid = room.lastMessage?.creator?.userId ?: 0L
        return when (room.type) {
            ROOM_TYPE_DIALOG -> if (isMyLastMessage(creatorUid)) context.getString(R.string.me) else String.empty()
            ROOM_TYPE_GROUP -> "${room.lastMessage?.creator?.name}"
            else -> String.empty()
        }
    }

    private fun composeCellContent(
        author: CharSequence? = null,
        content: CharSequence? = null,
        draft: CharSequence? = null,
        recognizedAudio: CharSequence? = null,
    ): Spannable {
        val builder = SpannableStringBuilder()
        if (draft != null) {
            builder
                .color(context.getColor(com.meera.uikit.R.color.uiKitColorAccentWrong)) {
                    append(context.getString(com.meera.uikit.R.string.draft))
                }
                .append(SPACE_SYMBOL)
                .append(draft)
        }
        if (!author.isNullOrBlank()) {
            builder
                .append(author)
                .append(DOUBLE_DOTS)
                .append(SPACE_SYMBOL)
        }
        if (recognizedAudio != null) {
            val imageSpan =
                object : ImageSpan(context, com.meera.uikit.R.drawable.ic_outlined_mic_s) {
                    override fun draw(
                        canvas: Canvas,
                        text: CharSequence?,
                        start: Int,
                        end: Int,
                        x: Float,
                        top: Int,
                        y: Int,
                        bottom: Int,
                        paint: Paint
                    ) {
                        val icon = drawable
                        canvas.save()
                        canvas.translate(x, dpToPx(ICON_SPACING).toFloat())
                        icon.setTint(context.getColor(com.meera.uikit.R.color.uiKitColorForegroundSecondary))
                        icon.draw(canvas)
                        canvas.restore()
                    }
                }
            builder.inSpans(imageSpan) { append(SPACE_SYMBOL) }
                .append(SPACE_SYMBOL)
                .append(recognizedAudio)
        }
        if (content != null) {
            builder.append(content)
        }
        return builder
    }

    private fun getMyUid() = getUserUidUseCase.invoke()

    private fun isMyLastMessage(creatorUid: Long) = getMyUid() == creatorUid
}
