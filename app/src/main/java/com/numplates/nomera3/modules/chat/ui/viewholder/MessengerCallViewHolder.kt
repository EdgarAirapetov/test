package com.numplates.nomera3.modules.chat.ui.viewholder

import android.view.View
import com.meera.uikit.widgets.chat.call.UiKitCallView
import com.meera.uikit.widgets.chat.highlight.TintBackgroundHighlighter
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.chat.helpers.replymessage.ISwipeableHolder
import com.numplates.nomera3.modules.chat.ui.highlight.Highlightable
import com.numplates.nomera3.modules.chat.ui.listeners.MeeraMessagesListenersMapper
import com.numplates.nomera3.modules.chat.ui.model.ChatMessageDataUiModel
import com.numplates.nomera3.modules.chat.ui.model.MessageConfigWrapperUiModel
import com.numplates.nomera3.modules.chat.ui.model.MessageWrapperUiListeners

class MessengerCallViewHolder(
    itemView: View,
    listenersMapper: MeeraMessagesListenersMapper,
) : BaseMessageViewHolder(itemView), ISwipeableHolder, Highlightable {

    private val callView = itemView as UiKitCallView

    init {
        val listener = listenersMapper.mapCallListeners(::getMessage) as MessageWrapperUiListeners.CallListeners
        callView.setListeners(listener.cell)
        bindContainerListener(listener.container)
    }

    override fun getSwipeContainer(): View = itemView

    override fun highlight() {
        callView.highlight(TintBackgroundHighlighter(tintColor = itemView.context.getColor(R.color.uiKitColorGradientMiddle)))
    }

    override fun bind(item: ChatMessageDataUiModel) {
        super.bind(item)
        callView.setConfig((item.messageConfig as MessageConfigWrapperUiModel.Call).config)
    }
}
