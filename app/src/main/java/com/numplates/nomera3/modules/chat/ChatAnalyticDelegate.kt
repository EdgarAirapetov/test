package com.numplates.nomera3.modules.chat

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import com.meera.core.extensions.empty
import com.meera.core.utils.files.FileManager
import com.meera.core.utils.files.FileUtilsImpl.Companion.MEDIA_TYPE_IMAGE_GIF
import com.meera.db.models.dialog.DialogEntity
import com.meera.db.models.dialog.UserChat
import com.meera.db.models.message.MessageEntity
import com.numplates.nomera3.CHAT_VOICE_MESSAGES_PATH
import com.numplates.nomera3.FRIEND_STATUS_CONFIRMED
import com.numplates.nomera3.USER_SUBSCRIBED
import com.numplates.nomera3.data.newmessenger.ROOM_TYPE_DIALOG
import com.numplates.nomera3.data.newmessenger.ROOM_TYPE_GROUP
import com.numplates.nomera3.data.newmessenger.TYPING_TYPE_AUDIO
import com.numplates.nomera3.data.newmessenger.TYPING_TYPE_GIF
import com.numplates.nomera3.data.newmessenger.TYPING_TYPE_IMAGE
import com.numplates.nomera3.data.newmessenger.TYPING_TYPE_VIDEO
import com.numplates.nomera3.domain.interactornew.GetUserUidUseCase
import com.numplates.nomera3.modules.analytics.domain.AnalyticsInteractor
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyActionTypeChatRequest
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyChatCreatedFromWhere
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyChatMediaKeyboardCategory
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyChatType
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyChatUserChatStatus
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyHaveMedia
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyNameConst
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyRecognizedTextButton
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhere
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhereCommunityOpen
import com.numplates.nomera3.modules.baseCore.helper.amplitude.chat.AmplitudeChatAnalytic
import com.numplates.nomera3.modules.baseCore.helper.amplitude.chat.AnalyticMessageParams
import com.numplates.nomera3.modules.baseCore.helper.amplitude.followbutton.AmplitudeFollowButton
import com.numplates.nomera3.modules.baseCore.helper.amplitude.followbutton.AmplitudeFollowButtonPropertyWhere
import com.numplates.nomera3.modules.baseCore.helper.amplitude.followbutton.AmplitudePropertyType
import com.numplates.nomera3.modules.baseCore.helper.amplitude.profile.AmplitudeInfluencerProperty
import com.numplates.nomera3.presentation.model.enums.ChatEventEnum
import timber.log.Timber
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ChatAnalyticDelegate @Inject constructor(
    private val analyticsInteractor: AnalyticsInteractor,
    private val chatAnalytic: AmplitudeChatAnalytic,
    private val amplitudeFollowButton: AmplitudeFollowButton,
    private var getUserUidUseCase: GetUserUidUseCase,
    private val fileManager: FileManager,
    private val context: Context
) {

    fun followAction(
        toId: Long,
        amplitudeInfluencerProperty: AmplitudeInfluencerProperty
    ) = amplitudeFollowButton.followAction(
        fromId = getUserUidUseCase.invoke(),
        toId = toId,
        where = AmplitudeFollowButtonPropertyWhere.CHAT,
        type = AmplitudePropertyType.OTHER,
        amplitudeInfluencerProperty = amplitudeInfluencerProperty
    )

    fun onBlockedUser(userId: Long, blockedUserId: Long) = analyticsInteractor.logBlockUser(userId, blockedUserId)

    fun onMessageResendMenuShowed() = chatAnalytic.onMessageResendMenuShowed()

    fun onMessageResendClicked(message: MessageEntity, roomType: String, companion: UserChat?) {
        chatAnalytic.onMessageResendClicked(getAnalyticMessageParams(message, roomType, companion))
    }

    fun onDeletedUnsentMessageClicked(message: MessageEntity, roomType: String, companion: UserChat?) {
        chatAnalytic.onDeletedUnsentMessageClicked(getAnalyticMessageParams(message, roomType, companion))
    }

    fun trackBanChatRequest(to: Long?) {
        logChatRequest(
            from = getUserUidUseCase.invoke(),
            to = to ?: -1,
            AmplitudePropertyActionTypeChatRequest.BAN
        )
    }

    fun logAmplitudeSendVideoMessage(
        text: String,
        roomType: String?,
        userId: Long,
        userChat: UserChat?,
        msgId: String
    ) {
        analyticsInteractor.logMessageSend(
            haveText = text.isNotEmpty(),
            havePic = false,
            haveVideo = true,
            haveAudio = false,
            duration = AmplitudePropertyNameConst.MESSAGE_DURATION_NONE,
            haveGif = false,
            haveMedia = AmplitudePropertyHaveMedia.YES,
            isGroupChat = roomType == ROOM_TYPE_GROUP,
            from = getUserUidUseCase.invoke(),
            to = userId,
            status = getChatUserStatus(userChat),
            messageId = msgId,
            mediaKeyboardCategory = AmplitudePropertyChatMediaKeyboardCategory.GALLERY
        )
    }

    fun logAmplitudeSendVoiceMessage(
        roomType: String?,
        userId: Long,
        durationSec: Long?,
        userChat: UserChat?,
        msgId: String
    ) {
        analyticsInteractor.logMessageSend(
            haveText = false,
            havePic = false,
            haveVideo = false,
            haveAudio = true,
            duration = durationSec,
            haveGif = false,
            haveMedia = AmplitudePropertyHaveMedia.YES,
            isGroupChat = roomType == ROOM_TYPE_GROUP,
            from = getUserUidUseCase.invoke(),
            to = userId,
            status = getChatUserStatus(userChat),
            messageId = msgId,
            mediaKeyboardCategory = AmplitudePropertyChatMediaKeyboardCategory.NONE
        )
    }

    fun logChatGifButtonPress() = analyticsInteractor.logChatGifButtonPress()

    fun logGroupChatDelete() = analyticsInteractor.logGroupChatDelete()

    fun logForwardMessageClicked() = analyticsInteractor.logForwardMessageClicked()

    fun unsentMessageCopy() = chatAnalytic.unsentMessageCopy()

    fun logSendGiftBack() = analyticsInteractor.logSendGiftBack()

    fun logUnlockChat(from: Long, to: Long) = analyticsInteractor.logUnlockChat(from = from, to = to)

    fun logAmplitudeStartChat(dialog: DialogEntity?, where: AmplitudePropertyChatCreatedFromWhere) {
        dialog?.let { room ->
            if (room.type == ROOM_TYPE_DIALOG) {
                analyticsInteractor.logTetATetChatCreated(
                    userId = getUserUidUseCase.invoke(),
                    companionUserId = dialog.companion.userId ?: 0,
                    status = getChatUserStatus(dialog.companion),
                    where = where
                )
            }
        }
    }

    fun logVoiceMessageRecognitionTap(message: MessageEntity, isExpanded: Boolean) {
        val fileUrl = message.attachment.url
        val fileName = Uri.parse(fileUrl).lastPathSegment
        val storageDir = File(
            context.getExternalFilesDir(null),
            "$CHAT_VOICE_MESSAGES_PATH/${message.roomId}"
        )
        val durationStr = getFileDuration(storageDir, fileName)
        val type = if (isExpanded) AmplitudePropertyRecognizedTextButton.OPEN
        else AmplitudePropertyRecognizedTextButton.CLOSE
        analyticsInteractor.logVoiceMessageRecognitionTap(message.msgId, type, TimeUnit.MILLISECONDS.toSeconds(durationStr))
    }

    fun onAllMessagesResend(messageCount: Int) = chatAnalytic.onAllMessagesResend(messageCount)

    fun logChatOpen(chatType: AmplitudePropertyChatType, openedFromWhere: AmplitudePropertyWhere) {
        analyticsInteractor.logChatOpen(chatType, openedFromWhere)
    }

    fun logCommunityScreenOpened() =
        analyticsInteractor.logCommunityScreenOpened(AmplitudePropertyWhereCommunityOpen.CHAT)

    fun logAmplitudeSendMessage(
        text: String,
        images: List<Uri>?,
        roomType: String?,
        userId: Long,
        userChat: UserChat?,
        msgId: String,
        mediaKeyboardCategory: AmplitudePropertyChatMediaKeyboardCategory = AmplitudePropertyChatMediaKeyboardCategory.NONE
    ) {

        val haveGif = if (!images.isNullOrEmpty()) {
            fileManager.getMediaType(images[0]) == MEDIA_TYPE_IMAGE_GIF
        } else {
            false
        }

        val havePic = if (haveGif) false else !images.isNullOrEmpty()

        val haveMedia = if (images.isNullOrEmpty())
            AmplitudePropertyHaveMedia.NO else AmplitudePropertyHaveMedia.YES

        analyticsInteractor.logMessageSend(
            haveText = text.isNotEmpty(),
            havePic = havePic,
            haveVideo = false,
            haveGif = haveGif,
            haveAudio = false,
            duration = AmplitudePropertyNameConst.MESSAGE_DURATION_NONE,
            haveMedia = haveMedia,
            isGroupChat = roomType == ROOM_TYPE_GROUP,
            from = getUserUidUseCase.invoke(),
            to = userId,
            status = getChatUserStatus(userChat),
            messageId = msgId,
            mediaKeyboardCategory = mediaKeyboardCategory
        )
    }

    fun trackAllowChatRequest(companionUid: Long?, withSendMessage: Boolean){
        if (withSendMessage) {
            logChatRequest(
                from = getUserUidUseCase.invoke(),
                to = companionUid,
                actionType = AmplitudePropertyActionTypeChatRequest.MESSAGE
            )
        } else {
            logChatRequest(
                from = getUserUidUseCase.invoke(),
                to = companionUid,
                actionType = AmplitudePropertyActionTypeChatRequest.ALLOW
            )
        }
    }

    fun trackMessageUnBlur(message: MessageEntity?) {
        message?.let { msg ->
            var photoCount = 0
            var gifCount = 0
            var videoCount = 0

            when (msg.eventCode) {
                ChatEventEnum.TEXT.state -> photoCount = msg.attachments.size
                ChatEventEnum.IMAGE.state -> photoCount = 1
                ChatEventEnum.GIF.state -> gifCount = 1
                ChatEventEnum.VIDEO.state -> videoCount = 1
            }
            chatAnalytic.onBlurMediaShowChatRequest(photoCount, gifCount, videoCount)
        }
    }

    private fun logChatRequest(from: Long?, to: Long?, actionType: AmplitudePropertyActionTypeChatRequest){
        analyticsInteractor.logChatRequest(
            fromUid = from ?: -1,
            toUid = to ?: -1,
            actionType
        )
    }

    private fun getChatUserStatus(companion: UserChat?): AmplitudePropertyChatUserChatStatus {
        return when {
            companion?.settingsFlags?.friendStatus == FRIEND_STATUS_CONFIRMED ->
                AmplitudePropertyChatUserChatStatus.FRIEND
            companion?.settingsFlags?.subscription_on == USER_SUBSCRIBED &&
                companion.settingsFlags?.subscribedToMe == USER_SUBSCRIBED ->
                AmplitudePropertyChatUserChatStatus.MUTUAL_FOLLOW
            companion?.settingsFlags?.subscribedToMe == USER_SUBSCRIBED -> AmplitudePropertyChatUserChatStatus.FOLLOWER
            companion?.settingsFlags?.subscription_on == USER_SUBSCRIBED -> AmplitudePropertyChatUserChatStatus.FOLLOW
            else -> AmplitudePropertyChatUserChatStatus.NOBODY
        }
    }

    private fun getAnalyticMessageParams(
        message: MessageEntity,
        roomType: String,
        companion: UserChat?,
    ) = AnalyticMessageParams(
        haveText = message.content.isNotEmpty(),
        havePic = message.metadata?.type != null && message.metadata?.type == TYPING_TYPE_IMAGE,
        haveVideo = message.metadata?.type != null && message.metadata?.type == TYPING_TYPE_VIDEO,
        haveGif = message.metadata?.type != null && message.metadata?.type == TYPING_TYPE_GIF,
        haveAudio = message.metadata?.type != null && message.metadata?.type == TYPING_TYPE_AUDIO,
        haveMedia = message.metadata?.let { it.type != null && message.type.isNotEmpty() } ?: false,
        groupChat = roomType == ROOM_TYPE_GROUP,
        from = message.creator?.userId.toString(),
        to = message.creator?.userId?.let { id ->
            if (id != companion?.userId) {
                companion?.userId.toString()
            } else {
                id.toString()
            }
        } ?: String.empty()
    )

    private fun getFileDuration(storageDir: File, fileName: String?) = try {
        val audioFile = File(storageDir, fileName)
        val mediaRetriever = MediaMetadataRetriever()
        mediaRetriever.setDataSource(context, Uri.parse(audioFile.absolutePath))
        mediaRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLong() ?: 0
    } catch (e: Exception) {
        Timber.e(e)
        -1
    }
}
