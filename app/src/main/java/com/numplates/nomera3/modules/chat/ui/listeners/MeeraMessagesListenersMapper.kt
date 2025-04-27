package com.numplates.nomera3.modules.chat.ui.listeners

import android.view.View
import com.google.gson.Gson
import com.meera.core.extensions.fromJson
import com.meera.core.extensions.isNotTrue
import com.meera.core.extensions.isTrue
import com.meera.core.extensions.toBoolean
import com.meera.db.models.userprofile.UserSimple
import com.meera.uikit.widgets.ListenersBucket
import com.meera.uikit.widgets.adult.UiKitAdultOverlayListeners
import com.meera.uikit.widgets.chat.container.UiKitMessagesContainerListeners
import com.meera.uikit.widgets.chat.emoji.UiKitEmojiListeners
import com.meera.uikit.widgets.chat.forward.UiKitForwardListeners
import com.meera.uikit.widgets.chat.gift.UiKitGiftListeners
import com.meera.uikit.widgets.chat.header.UiKitHeaderListeners
import com.meera.uikit.widgets.chat.media.UiKitMediaListeners
import com.meera.uikit.widgets.chat.moment.UiKitMomentListeners
import com.meera.uikit.widgets.chat.profile.UiKitShareProfileListeners
import com.meera.uikit.widgets.chat.regular.UiKitRegularListeners
import com.meera.uikit.widgets.chat.reply.UiKitReplyListeners
import com.meera.uikit.widgets.chat.repost.UiKitRepostListeners
import com.meera.uikit.widgets.chat.sticker.UiKitStickerListeners
import com.meera.uikit.widgets.chat.voice.UiKitDetectorSeekBar
import com.meera.uikit.widgets.chat.voice.UiKitVoiceListeners
import com.meera.uikit.widgets.chat.voice.UiKitVoiceView
import com.meera.uikit.widgets.chat.voice.VoiceButtonState
import com.meera.uikit.widgets.disclaimer.ResourceConfig
import com.meera.uikit.widgets.disclaimer.UiKitDisclaimerListeners
import com.meera.uikit.widgets.disclaimer.UiKitDisclaimerView
import com.numplates.nomera3.modules.chat.ChatPayloadKeys
import com.numplates.nomera3.modules.chat.helpers.voicemessage.updateChatVoiceButtonConfig
import com.numplates.nomera3.modules.chat.ui.model.ChatMessageDataUiModel
import com.numplates.nomera3.modules.chat.ui.model.MessageWrapperUiListeners
import com.numplates.nomera3.modules.communities.data.entity.CommunityShareEntity
import com.numplates.nomera3.presentation.view.adapter.newchat.chatimage.PostImage

class MeeraMessagesListenersMapper(
    private val messagesListener: MeeraMessagesListener,
    private val gson: Gson = Gson()
) {

    @Suppress("unused")
    fun mapEmptyListeners(): MessageWrapperUiListeners {
        return MessageWrapperUiListeners.EmptyListener
    }

    fun mapCallListeners(fetchMessage: () -> ChatMessageDataUiModel?): MessageWrapperUiListeners {
        return MessageWrapperUiListeners.CallListeners(
            cell = null,
            container = provideMessageContainerListeners(fetchMessage),
        )
    }

    fun mapTextListeners(fetchMessage: () -> ChatMessageDataUiModel?): MessageWrapperUiListeners {
        return MessageWrapperUiListeners.RegularListeners(
            cell = UiKitRegularListeners(
                header = provideHeaderListener(fetchMessage),
                reply = provideReplyListener(fetchMessage),
                forward = provideForwardListener(fetchMessage)
            ),
            container = provideMessageContainerListeners(fetchMessage),
        )
    }

    fun mapEmojiListener(fetchMessage: () -> ChatMessageDataUiModel?): MessageWrapperUiListeners {
        return MessageWrapperUiListeners.EmojiListeners(
            cell = UiKitEmojiListeners(
                reply = provideReplyListener(fetchMessage),
                forward = provideForwardListener(fetchMessage)
            ),
            container = provideMessageContainerListeners(fetchMessage),
        )
    }

    fun mapAudioListeners(
        cell: UiKitVoiceView,
        fetchPosition: () -> Int,
        fetchMessage: () -> ChatMessageDataUiModel?
    ): MessageWrapperUiListeners {
        return MessageWrapperUiListeners.AudioListeners(
            cell = UiKitVoiceListeners(
                header = provideHeaderListener(fetchMessage),
                reply = provideReplyListener(fetchMessage),
                forward = provideForwardListener(fetchMessage),
                seekBarListener = object : UiKitDetectorSeekBar.IListener {
                    override fun onClick(detectorSeekBar: UiKitDetectorSeekBar?) {
                        val progress = detectorSeekBar?.progress ?: 0
                        messagesListener.onClickMeeraVoiceMessageProgress(cell, progress)
                    }

                    override fun onLongClick(detectorSeekBar: UiKitDetectorSeekBar?) = Unit
                },
                buttonListener = object : UiKitVoiceView.BtnPrimaryClickListener {
                    override fun onDownloadAudioClicked() {
                        messagesListener.onDownloadClickMeeraVoiceMessage(
                            cell = cell,
                            data = fetchMessage.invoke(),
                        )
                    }

                    override fun onPauseAudioClicked() {
                        fetchMessage.invoke()?.let { message ->
                            updateChatVoiceButtonConfig(cell, message, VoiceButtonState.Default)
                        }
                        messagesListener.onPauseClickMeeraVoiceMessage(
                            cell = cell,
                            data = fetchMessage.invoke(),
                            position = fetchPosition.invoke()
                        )
                    }

                    override fun onPlayAudioClicked() {
                        fetchMessage.invoke()?.let { message ->
                            updateChatVoiceButtonConfig(cell, message, VoiceButtonState.Pause)
                        }
                        messagesListener.onPlayClickMeeraVoiceMessage(
                            cell = cell,
                            data = fetchMessage.invoke(),
                            position = fetchPosition.invoke()
                        )

                    }

                    override fun onStopDownloadingClicked() {
                        fetchMessage.invoke()?.let { message ->
                            updateChatVoiceButtonConfig(cell, message, VoiceButtonState.Download)
                        }
                        messagesListener.onStopDownloadClickMeeraVoiceMessage(
                            cell = cell,
                            data = fetchMessage.invoke()
                        )
                    }
                }
            ),
            container = provideMessageContainerListeners(
                fetchMessage = fetchMessage,
                longClick = { view ->
                    val messageData = fetchMessage()?.messageData
                    messagesListener.onVoiceMessageLongClicked(
                        messageId = messageData?.id,
                        messageView = view,
                        recognizedText = messageData?.attachments?.attachments?.firstOrNull()?.audioRecognizedText,
                    )
                    true
                },
            )
        )
    }

    fun mapStickerListeners(fetchMessage: () -> ChatMessageDataUiModel?): MessageWrapperUiListeners {
        return MessageWrapperUiListeners.StickerListeners(
            cell = UiKitStickerListeners(
                reply = provideReplyListener(fetchMessage),
                forward = provideForwardListener(fetchMessage)
            ),
            container = provideMessageContainerListeners(fetchMessage),
        )
    }

    fun mapRepostListeners(fetchMessage: () -> ChatMessageDataUiModel?): MessageWrapperUiListeners {
        return MessageWrapperUiListeners.RepostListeners(
            cell = UiKitRepostListeners(
                header = provideHeaderListener(fetchMessage),
                reply = provideReplyListener(fetchMessage),
                forward = provideForwardListener(fetchMessage),
                adultOverlayListeners = UiKitAdultOverlayListeners(
                    actionButton = ListenersBucket(
                        click = {
                            val repostMap =
                                fetchMessage.invoke()?.messageData?.attachments?.attachments?.firstOrNull()?.repost
                            val postId = repostMap?.get(ChatPayloadKeys.ID.key)?.toString()?.toDouble()?.toLong()
                            postId?.let(messagesListener::onShowPostClicked)
                        }
                    )
                )
            ),
            container = provideMessageContainerListeners(
                fetchMessage = fetchMessage,
                click = {
                    val repostMap = fetchMessage.invoke()?.messageData?.attachments?.attachments?.firstOrNull()?.repost
                    val postId = repostMap?.get(ChatPayloadKeys.ID.key)?.toString()?.toDouble()?.toLong()
                    postId?.let(messagesListener::onShowMoreRepost)
                }
            ),
        )
    }

    fun mapGiftListeners(fetchMessage: () -> ChatMessageDataUiModel?): MessageWrapperUiListeners {
        return MessageWrapperUiListeners.GiftListeners(
            cell = UiKitGiftListeners(
                reply = provideReplyListener(fetchMessage),
                forward = provideForwardListener(fetchMessage),
                button = ListenersBucket(
                    click = { fetchMessage.invoke()?.messageData?.creator?.id?.let(messagesListener::onChooseGiftClicked) }
                )
            ),
            container = provideMessageContainerListeners(
                fetchMessage = fetchMessage,
                click = {
                    val message = fetchMessage.invoke()
                    if (message?.messageData?.isMy.isNotTrue()) {
                        message?.messageData?.creator?.id?.let(messagesListener::onReceiverGiftClicked)
                    } else {
                        messagesListener.onSenderGiftClicked()
                    }
                }
            ),
        )
    }

    fun mapMomentListeners(fetchMessage: () -> ChatMessageDataUiModel?): MessageWrapperUiListeners {
        return MessageWrapperUiListeners.MomentListeners(
            cell = UiKitMomentListeners(),
            container = provideMessageContainerListeners(
                fetchMessage = fetchMessage,
                click = {
                    val momentMap = fetchMessage.invoke()?.messageData?.attachments?.attachments?.firstOrNull()?.moment
                    val momentId = momentMap?.get(ChatPayloadKeys.ID.key)?.toString()?.toDouble()?.toLong()
                    momentId?.let(messagesListener::onShowRepostMoment)
                }
            ),
        )
    }

    fun mapDeletedListeners(fetchMessage: () -> ChatMessageDataUiModel?): MessageWrapperUiListeners {
        return MessageWrapperUiListeners.DeletedListeners(
            cell = {},
            container = provideMessageContainerListeners(fetchMessage),
        )
    }

    fun mapMediaListeners(fetchMessage: () -> ChatMessageDataUiModel?): MessageWrapperUiListeners {
        return MessageWrapperUiListeners.MediaListeners(
            cell = UiKitMediaListeners(
                header = provideHeaderListener(fetchMessage),
                reply = provideReplyListener(fetchMessage),
                forward = provideForwardListener(fetchMessage),
                disclaimer = UiKitDisclaimerListeners(
                    media = object : UiKitDisclaimerView.MediaListener {
                        override fun mediaLongClicked(view: View, resource: ResourceConfig) {
                            messagesListener.onMessageLongClicked(fetchMessage.invoke()?.messageData?.id, view)
                        }

                        override fun mediaClicked(view: View, resource: ResourceConfig) {
                            val messageData = fetchMessage.invoke()?.messageData ?: return
                            val mediaData = messageData.attachments?.attachments.orEmpty().map {
                                PostImage(
                                    url = it.url,
                                    isShowGiphyWatermark = messageData.isShowGiphyWatermark.isTrue()
                                )
                            }
                            messagesListener.onImageClicked(
                                messageId = messageData.id,
                                data = mediaData,
                                childPosition = mediaData.indexOfFirst { it.url == resource.url }
                            )
                        }
                    },
                    button = ListenersBucket(
                        click = {
                            messagesListener.disableImageBlur(fetchMessage.invoke()?.messageData?.id)
                        }
                    ),
                )
            ),
            container = provideMessageContainerListeners(fetchMessage),
        )
    }

    fun mapShareProfileListeners(fetchMessage: () -> ChatMessageDataUiModel?): MessageWrapperUiListeners {
        return MessageWrapperUiListeners.ShareProfileListeners(
            cell = UiKitShareProfileListeners(
                header = provideHeaderListener(fetchMessage),
                reply = provideReplyListener(fetchMessage),
                forward = provideForwardListener(fetchMessage),
            ),
            container = provideMessageContainerListeners(
                fetchMessage = fetchMessage,
                click = {
                    val metadata = fetchMessage.invoke()?.messageData?.attachments?.attachments?.firstOrNull()?.metadata
                    val user = gson.fromJson<UserSimple>(metadata ?: return@provideMessageContainerListeners)
                    messagesListener.onAvatarClicked(user.userId)
                }
            ),
        )
    }

    fun mapShareCommunityListeners(fetchMessage: () -> ChatMessageDataUiModel?): MessageWrapperUiListeners {
        return MessageWrapperUiListeners.ShareCommunityListeners(
            cell = UiKitShareProfileListeners(
                header = provideHeaderListener(fetchMessage),
                reply = provideReplyListener(fetchMessage),
                forward = provideForwardListener(fetchMessage)
            ),
            container = provideMessageContainerListeners(
                fetchMessage = fetchMessage,
                click = {
                    val metadata = fetchMessage.invoke()?.messageData?.attachments?.attachments?.firstOrNull()?.metadata
                    val group = gson.fromJson<CommunityShareEntity>(metadata ?: return@provideMessageContainerListeners)
                    messagesListener.onCommunityClicked(
                        groupId = group.id,
                        isDeleted = group.deleted.toBoolean()
                    )
                }
            ),
        )
    }

    fun mapGreetingListeners(fetchMessage: () -> ChatMessageDataUiModel?): MessageWrapperUiListeners {
        return MessageWrapperUiListeners.GreetingListeners(
            cell = {},
            container = provideMessageContainerListeners(fetchMessage),
        )
    }

    private fun provideMessageContainerListeners(
        fetchMessage: () -> ChatMessageDataUiModel?,
        longClick: (View) -> Boolean = { view ->
            messagesListener.onMessageLongClicked(fetchMessage.invoke()?.messageData?.id, view)
            true
        },
        click: (View) -> Unit = {}
    ): UiKitMessagesContainerListeners {
        return UiKitMessagesContainerListeners(
            container = ListenersBucket(longClick = longClick, click = click),
            userpick = ListenersBucket(click = {
                fetchMessage.invoke()?.messageData?.creator?.id?.let(messagesListener::onAvatarClicked)
            })
        )
    }

    private fun provideHeaderListener(
        fetchMessage: () -> ChatMessageDataUiModel?,
        bubbleBucket: ListenersBucket? = null,
        headerBucket: ListenersBucket = ListenersBucket(click = {
            fetchMessage.invoke()?.messageData?.creator?.id?.let(messagesListener::onAvatarClicked)
        })
    ): UiKitHeaderListeners {
        return UiKitHeaderListeners(bubble = bubbleBucket, header = headerBucket)
    }

    private fun provideReplyListener(
        fetchMessage: () -> ChatMessageDataUiModel?,
        longClick: (View) -> Boolean = { true },
        click: (View) -> Unit = {
            fetchMessage.invoke()?.messageData?.id?.let(messagesListener::onClickReplyParentMessage)
        },
    ): UiKitReplyListeners {
        return UiKitReplyListeners(bubble = ListenersBucket(longClick = longClick, click = click))
    }

    private fun provideForwardListener(
        fetchMessage: () -> ChatMessageDataUiModel?,
        longClick: (View) -> Boolean = { true },
        click: (View) -> Unit = {
            fetchMessage.invoke()?.messageData?.creator?.id?.let(messagesListener::onAvatarClicked)
        }
    ): UiKitForwardListeners {
        return UiKitForwardListeners(bubble = ListenersBucket(longClick = longClick, click = click))
    }
}
