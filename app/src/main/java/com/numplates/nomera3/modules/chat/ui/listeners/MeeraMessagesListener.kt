package com.numplates.nomera3.modules.chat.ui.listeners

import android.view.View
import com.meera.db.models.message.MessageEntity
import com.meera.uikit.widgets.chat.voice.UiKitVoiceView
import com.numplates.nomera3.modules.chat.ui.model.ChatMessageDataUiModel
import com.numplates.nomera3.presentation.audio.VoiceMessageView
import com.numplates.nomera3.presentation.view.adapter.newchat.chatimage.PostImage

interface MeeraMessagesListener {

    fun onMessageLongClicked(messageId: String?, messageView: View? = null)

    fun onClickReplyParentMessage(messageId: String)

    fun onResendMessageClicked(messageId: String?)

    fun onAvatarClicked(userId: Long?)

    fun onCommunityClicked(groupId: Int?, isDeleted: Boolean)

    fun onAttachmentClicked(message: MessageEntity?)

    fun onShowMoreRepost(postId: Long)

    fun onShowRepostMoment(momentId: Long)

    fun onUniquenameClicked(userId: Long?)

    fun onUniquenameUnknownProfileError()

    fun onHashtagClicked(hashtag: String?)

    fun onImageClicked(messageId: String, data: List<PostImage>, childPosition: Int)

    fun onReceiverGiftClicked(myUid: Long)

    fun onSenderGiftClicked()

    fun onChooseGiftClicked(userId: Long)

    fun onSetNoMediaPlaceholderMessage(messageId: String)

    fun disableImageBlur(messageId: String?)

    fun onBirthdayTextClicked()

    fun onShowPostClicked(postId: Long?)

    fun onDeletedMessageLongClicked(messageId: String?)

    fun onLinkClicked(url: String?)

    /* VOICE MESSAGE */
    fun onVoicePlayClicked(
        message: MessageEntity?,
        position: Int
    )

    fun onVoiceMessageLongClicked(
        messageId: String?,
        messageView: View? = null,
        recognizedText: String? = null
    )

    fun onVoiceMessagebinded(
        voiceMessageView: VoiceMessageView?,
        message: MessageEntity,
        isIncomingMessage: Boolean
    ) {
    }

    fun onExpandVoiceMessageText(message: MessageEntity, isExpanded: Boolean)

    fun onExpandBtnAnimationCompleteVoiceMessage(message: MessageEntity)

    fun onBindMeeraVoiceMessage(cell: UiKitVoiceView, data: ChatMessageDataUiModel, isAudioFileExists: Boolean)

    fun onPlayClickMeeraVoiceMessage(cell: UiKitVoiceView, data: ChatMessageDataUiModel?, position: Int)

    fun onPauseClickMeeraVoiceMessage(cell: UiKitVoiceView, data: ChatMessageDataUiModel?, position: Int)

    fun onDownloadClickMeeraVoiceMessage(cell: UiKitVoiceView, data: ChatMessageDataUiModel?)

    fun onStopDownloadClickMeeraVoiceMessage(cell: UiKitVoiceView, data: ChatMessageDataUiModel?)

    fun onClickMeeraVoiceMessageProgress(cell: UiKitVoiceView, progress: Int)
}
