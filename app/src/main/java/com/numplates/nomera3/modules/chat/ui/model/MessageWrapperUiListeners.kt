package com.numplates.nomera3.modules.chat.ui.model

import com.meera.uikit.widgets.chat.call.UiKitCallListeners
import com.meera.uikit.widgets.chat.container.UiKitMessagesContainerListeners
import com.meera.uikit.widgets.chat.emoji.UiKitEmojiListeners
import com.meera.uikit.widgets.chat.gift.UiKitGiftListeners
import com.meera.uikit.widgets.chat.media.UiKitMediaListeners
import com.meera.uikit.widgets.chat.moment.UiKitMomentListeners
import com.meera.uikit.widgets.chat.profile.UiKitShareProfileListeners
import com.meera.uikit.widgets.chat.regular.UiKitRegularListeners
import com.meera.uikit.widgets.chat.repost.UiKitRepostListeners
import com.meera.uikit.widgets.chat.sticker.UiKitStickerListeners
import com.meera.uikit.widgets.chat.voice.UiKitVoiceListeners

sealed interface MessageWrapperUiListeners {

    data object EmptyListener : MessageWrapperUiListeners

    data class RegularListeners(
        val cell: UiKitRegularListeners?,
        val container: UiKitMessagesContainerListeners?
    ) : MessageWrapperUiListeners

    data class EmojiListeners(
        val cell: UiKitEmojiListeners?,
        val container: UiKitMessagesContainerListeners?
    ) : MessageWrapperUiListeners

    data class CallListeners(
        val cell: UiKitCallListeners?,
        val container: UiKitMessagesContainerListeners?
    ) : MessageWrapperUiListeners

    data class AudioListeners(
        val cell: UiKitVoiceListeners?,
        val container: UiKitMessagesContainerListeners?
    ) : MessageWrapperUiListeners

    data class StickerListeners(
        val cell: UiKitStickerListeners?,
        val container: UiKitMessagesContainerListeners?
    ) : MessageWrapperUiListeners

    data class RepostListeners(
        val cell: UiKitRepostListeners?,
        val container: UiKitMessagesContainerListeners?
    ) : MessageWrapperUiListeners

    data class GiftListeners(
        val cell: UiKitGiftListeners?,
        val container: UiKitMessagesContainerListeners?
    ) : MessageWrapperUiListeners

    data class MomentListeners(
        val cell: UiKitMomentListeners?,
        val container: UiKitMessagesContainerListeners?
    ) : MessageWrapperUiListeners

    data class DeletedListeners(
        val cell: (() -> Unit),
        val container: UiKitMessagesContainerListeners?
    ) : MessageWrapperUiListeners

    data class MediaListeners(
        val cell: UiKitMediaListeners?,
        val container: UiKitMessagesContainerListeners?
    ) : MessageWrapperUiListeners

    data class ShareProfileListeners(
        val cell: UiKitShareProfileListeners?,
        val container: UiKitMessagesContainerListeners?
    ) : MessageWrapperUiListeners

    data class ShareCommunityListeners(
        val cell: UiKitShareProfileListeners?,
        val container: UiKitMessagesContainerListeners?
    ) : MessageWrapperUiListeners

    data class GreetingListeners(
        val cell: (() -> Unit),
        val container: UiKitMessagesContainerListeners?
    ) : MessageWrapperUiListeners
}
