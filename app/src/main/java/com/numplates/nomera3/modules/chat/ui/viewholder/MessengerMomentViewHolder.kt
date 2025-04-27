package com.numplates.nomera3.modules.chat.ui.viewholder

import android.view.View
import com.meera.uikit.widgets.chat.highlight.TintBackgroundHighlighter
import com.meera.uikit.widgets.chat.moment.UiKitMomentView
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.chat.helpers.replymessage.ISwipeableHolder
import com.numplates.nomera3.modules.chat.ui.highlight.Highlightable
import com.numplates.nomera3.modules.chat.ui.listeners.MeeraMessagesListenersMapper
import com.numplates.nomera3.modules.chat.ui.listeners.MeeraSpanListenersMapper
import com.numplates.nomera3.modules.chat.ui.model.ChatMessageDataUiModel
import com.numplates.nomera3.modules.chat.ui.model.MessageConfigWrapperUiModel
import com.numplates.nomera3.modules.chat.ui.model.MessageWrapperUiListeners

class MessengerMomentViewHolder(
    itemView: View,
    listenersMapper: MeeraMessagesListenersMapper,
    private val meeraSpanListenersMapper: MeeraSpanListenersMapper,
) : BaseMessageViewHolder(itemView), ISwipeableHolder, Highlightable {

    private val momentView = itemView as UiKitMomentView

    init {
        val listeners = listenersMapper.mapMomentListeners(::getMessage) as MessageWrapperUiListeners.MomentListeners
        bindContainerListener(listeners.container)
        momentView.setListeners(listeners.cell)
    }

    override fun getSwipeContainer(): View = itemView

    override fun highlight() {
        momentView.highlight(TintBackgroundHighlighter(tintColor = itemView.context.getColor(R.color.uiKitColorGradientMiddle)))
    }

    override fun bind(item: ChatMessageDataUiModel) {
        super.bind(item)
        momentView.setConfig(
            (item.messageConfig as MessageConfigWrapperUiModel.Moment).config.copy(
                messageContent = meeraSpanListenersMapper.mapSpannableListeners(item.messageData)
            )
        )
    }
}
