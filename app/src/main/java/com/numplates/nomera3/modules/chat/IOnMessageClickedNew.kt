package com.numplates.nomera3.modules.chat

import android.view.View
import com.meera.db.models.dialog.UserChat
import com.meera.db.models.message.MessageEntity
import com.numplates.nomera3.presentation.audio.VoiceMessageView
import com.numplates.nomera3.presentation.view.adapter.newchat.chatimage.PostImage

interface IOnMessageClickedNew {

    fun onMessageLongClicked(message: MessageEntity?)

    fun onMessageLongClicked(message: MessageEntity?, messageView: View? = null)

    fun onSwipeReplyMessage(message: MessageEntity?)

    fun onClickReplyParentMessage(message: MessageEntity)

    fun onResendMessageClicked(message: MessageEntity?)

    fun onAvatarClicked(userId: Long?)

    fun onCommunityClicked(groupId: Int?, isDeleted: Boolean)

    fun onAttachmentClicked(message: MessageEntity?)

    fun onVoicePlayClicked(
        message: MessageEntity?,
        position: Int
    )

    fun onVoiceMessageLongClicked(
        message: MessageEntity?,
        messageView: View? = null,
        recognizedText: String? = null
    )

    fun onExpandVoiceMessageText(message: MessageEntity, isExpanded: Boolean)

    fun onExpandBtnAnimationCompleteVoiceMessage(message: MessageEntity)

    fun onShowMoreRepost(postId: Long)

    fun onShowRepostMoment(momentId: Long)

    fun onUniquenameClicked(userId: Long?, message: MessageEntity?)

    fun onUniquenameUnknownProfileError()

    fun onHashtagClicked(hashtag: String?)

    fun onImageClicked(message: MessageEntity?, data: List<PostImage>, childPosition: Int)

    fun onReceiverGiftClicked(myUid: Long)

    fun onSenderGiftClicked()

    fun onChooseGiftClicked(user: UserChat)

    fun onVoiceMessagebinded(
        voiceMessageView: VoiceMessageView?,
        message: MessageEntity,
        isIncomingMessage: Boolean
    ) { }

    fun onSetNoMediaPlaceholderMessage(message: MessageEntity)

    fun disableImageBlur(message: MessageEntity?)

    fun onBirthdayTextClicked()

    fun onShowPostClicked(postId: Long?)

    fun onDeletedMessageLongClicked(message: MessageEntity?)

    fun onLinkClicked(url: String?)
}
