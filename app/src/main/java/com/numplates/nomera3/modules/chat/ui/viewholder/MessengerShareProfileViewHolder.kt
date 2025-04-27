package com.numplates.nomera3.modules.chat.ui.viewholder

import android.view.View
import com.meera.uikit.widgets.chat.highlight.TintBackgroundHighlighter
import com.meera.uikit.widgets.chat.profile.UiKitShareProfileView
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.chat.helpers.replymessage.ISwipeableHolder
import com.numplates.nomera3.modules.chat.ui.highlight.Highlightable
import com.numplates.nomera3.modules.chat.ui.listeners.MeeraMessagesListenersMapper
import com.numplates.nomera3.modules.chat.ui.model.ChatMessageDataUiModel
import com.numplates.nomera3.modules.chat.ui.model.MessageConfigWrapperUiModel
import com.numplates.nomera3.modules.chat.ui.model.MessageWrapperUiListeners

class MessengerShareProfileViewHolder(
    itemView: View,
    listenersMapper: MeeraMessagesListenersMapper,
) : BaseMessageViewHolder(itemView), ISwipeableHolder, Highlightable {

    private val profileView = itemView as UiKitShareProfileView

    init {
        val listeners =
            listenersMapper.mapShareProfileListeners(::getMessage) as MessageWrapperUiListeners.ShareProfileListeners
        bindContainerListener(listeners.container)
        profileView.setListeners(listeners.cell)
    }

    override fun getSwipeContainer(): View = itemView

    override fun highlight() {
        profileView.highlight(TintBackgroundHighlighter(tintColor = itemView.context.getColor(R.color.uiKitColorGradientMiddle)))
    }

    override fun bind(item: ChatMessageDataUiModel) {
        super.bind(item)
        profileView.setConfig((item.messageConfig as MessageConfigWrapperUiModel.ShareProfile).config)
    }
}
