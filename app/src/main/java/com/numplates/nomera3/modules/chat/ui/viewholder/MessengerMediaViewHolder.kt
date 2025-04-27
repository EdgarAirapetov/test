package com.numplates.nomera3.modules.chat.ui.viewholder

import android.view.View
import com.meera.uikit.widgets.chat.highlight.ForegroundHighlighter
import com.meera.uikit.widgets.chat.highlight.TintBackgroundHighlighter
import com.meera.uikit.widgets.chat.media.UiKitMediaView
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.chat.helpers.replymessage.ISwipeableHolder
import com.numplates.nomera3.modules.chat.ui.highlight.Highlightable
import com.numplates.nomera3.modules.chat.ui.listeners.MeeraMessagesListenersMapper
import com.numplates.nomera3.modules.chat.ui.listeners.MeeraSpanListenersMapper
import com.numplates.nomera3.modules.chat.ui.model.ChatMessageDataUiModel
import com.numplates.nomera3.modules.chat.ui.model.MessageConfigWrapperUiModel
import com.numplates.nomera3.modules.chat.ui.model.MessageWrapperUiListeners

class MessengerMediaViewHolder(
    itemView: View,
    listenersMapper: MeeraMessagesListenersMapper,
    private val meeraSpanListenersMapper: MeeraSpanListenersMapper,
) : BaseMessageViewHolder(itemView), ISwipeableHolder, Highlightable {

    private val mediaView = itemView as UiKitMediaView

    init {
        val listeners = listenersMapper.mapMediaListeners(::getMessage) as MessageWrapperUiListeners.MediaListeners
        bindContainerListener(listeners.container)
        mediaView.setListeners(listeners.cell)
    }

    override fun getSwipeContainer(): View = itemView

    override fun highlight() {
        mediaView.highlight(
            highlighter = TintBackgroundHighlighter(tintColor = itemView.context.getColor(R.color.uiKitColorGradientMiddle)),
            disclaimerHighlighter = ForegroundHighlighter(color = itemView.context.getColor(R.color.uiKitColorFadePrimary40))
        )
    }

    override fun bind(item: ChatMessageDataUiModel) {
        super.bind(item)
        mediaView.setConfig(
            (item.messageConfig as MessageConfigWrapperUiModel.Media).config.copy(
                message = meeraSpanListenersMapper.mapSpannableListeners(item.messageData)
            )
        )
    }
}
