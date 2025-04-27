package com.numplates.nomera3.modules.chat.ui.viewholder

import android.view.View
import com.meera.uikit.widgets.chat.highlight.TintBackgroundHighlighter
import com.meera.uikit.widgets.chat.repost.UiKitRepostView
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.chat.helpers.replymessage.ISwipeableHolder
import com.numplates.nomera3.modules.chat.ui.highlight.Highlightable
import com.numplates.nomera3.modules.chat.ui.listeners.MeeraMessagesListenersMapper
import com.numplates.nomera3.modules.chat.ui.listeners.MeeraSpanListenersMapper
import com.numplates.nomera3.modules.chat.ui.model.ChatMessageDataUiModel
import com.numplates.nomera3.modules.chat.ui.model.MessageConfigWrapperUiModel
import com.numplates.nomera3.modules.chat.ui.model.MessageWrapperUiListeners

class MessengerRepostViewHolder(
    itemView: View,
    listenersMapper: MeeraMessagesListenersMapper,
    private val meeraSpanListenersMapper: MeeraSpanListenersMapper,
) : BaseMessageViewHolder(itemView), ISwipeableHolder, Highlightable {

    private val repostView = itemView as UiKitRepostView

    init {
        val listeners = listenersMapper.mapRepostListeners(::getMessage) as MessageWrapperUiListeners.RepostListeners
        bindContainerListener(listeners.container)
        repostView.setListeners(listeners.cell)
    }

    override fun getSwipeContainer(): View = itemView

    override fun highlight() {
        repostView.highlight(TintBackgroundHighlighter(tintColor = itemView.context.getColor(R.color.uiKitColorGradientMiddle)))
    }

    override fun bind(item: ChatMessageDataUiModel) {
        super.bind(item)

        repostView.setConfig(
            (item.messageConfig as MessageConfigWrapperUiModel.Repost).config.copy(
                messageContent = meeraSpanListenersMapper.mapSpannableListeners(item.messageData)
                    .takeIf { it.isNotBlank() },
            )
        )
    }
}
