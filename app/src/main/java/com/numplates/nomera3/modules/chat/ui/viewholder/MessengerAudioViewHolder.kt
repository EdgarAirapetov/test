package com.numplates.nomera3.modules.chat.ui.viewholder

import android.net.Uri
import android.view.View
import com.meera.core.extensions.empty
import com.meera.uikit.widgets.chat.highlight.TintBackgroundHighlighter
import com.meera.uikit.widgets.chat.voice.UiKitVoiceView
import com.meera.uikit.widgets.chat.voice.VoiceButtonState
import com.numplates.nomera3.CHAT_VOICE_MESSAGES_PATH
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.chat.helpers.replymessage.ISwipeableHolder
import com.numplates.nomera3.modules.chat.helpers.voicemessage.setChatVoiceButtonConfig
import com.numplates.nomera3.modules.chat.ui.highlight.Highlightable
import com.numplates.nomera3.modules.chat.ui.listeners.MeeraMessagesListener
import com.numplates.nomera3.modules.chat.ui.listeners.MeeraMessagesListenersMapper
import com.numplates.nomera3.modules.chat.ui.model.ChatMessageDataUiModel
import com.numplates.nomera3.modules.chat.ui.model.MessageWrapperUiListeners
import java.io.File

class MessengerAudioViewHolder(
    itemView: View,
    listenersMapper: MeeraMessagesListenersMapper,
    val messagesListener: MeeraMessagesListener
) : BaseMessageViewHolder(itemView), ISwipeableHolder, Highlightable {

    private val audioView = itemView as UiKitVoiceView

    init {
        val listeners = (listenersMapper.mapAudioListeners(
            audioView,
            ::getBindingAdapterPosition,
            ::getMessage
        ) as MessageWrapperUiListeners.AudioListeners)
        audioView.setListeners(listeners.cell)
        bindContainerListener(listeners.container)
    }

    override fun getSwipeContainer(): View = itemView

    override fun highlight() {
        audioView.highlight(TintBackgroundHighlighter(tintColor = itemView.context.getColor(R.color.uiKitColorGradientMiddle)))
    }

    override fun bind(item: ChatMessageDataUiModel) {
        super.bind(item)
        val message = item.messageData
        val fileUrl = message.attachments?.attachments?.first()?.url ?: String.empty()
        val fileName = Uri.parse(fileUrl).lastPathSegment ?: String.empty()
        val storageDir = File(
            itemView.context.getExternalFilesDir(null),
            "$CHAT_VOICE_MESSAGES_PATH/${message.roomId}"
        )

        val audioFile = File(storageDir, fileName)
        if (audioFile.exists()) {
            setChatVoiceButtonConfig(audioView, item, VoiceButtonState.Default)
            messagesListener.onBindMeeraVoiceMessage(cell = audioView, data = item, isAudioFileExists = true)
        } else {
            setChatVoiceButtonConfig(audioView, item, VoiceButtonState.Download)
            messagesListener.onBindMeeraVoiceMessage(cell = audioView, data = item, isAudioFileExists = false)
        }
    }

}
