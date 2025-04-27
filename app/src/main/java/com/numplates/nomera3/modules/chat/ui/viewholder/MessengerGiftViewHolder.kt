package com.numplates.nomera3.modules.chat.ui.viewholder

import android.view.View
import com.meera.uikit.widgets.chat.gift.UiKitGiftView
import com.numplates.nomera3.modules.chat.ui.listeners.MeeraMessagesListenersMapper
import com.numplates.nomera3.modules.chat.ui.listeners.MeeraSpanListenersMapper
import com.numplates.nomera3.modules.chat.ui.model.ChatMessageDataUiModel
import com.numplates.nomera3.modules.chat.ui.model.MessageConfigWrapperUiModel
import com.numplates.nomera3.modules.chat.ui.model.MessageWrapperUiListeners

class MessengerGiftViewHolder(
    itemView: View,
    listenersMapper: MeeraMessagesListenersMapper,
    private val meeraSpanListenersMapper: MeeraSpanListenersMapper,
) : BaseMessageViewHolder(itemView) {

    private val giftView = itemView as UiKitGiftView

    init {
        val listeners = listenersMapper.mapGiftListeners(::getMessage) as MessageWrapperUiListeners.GiftListeners
        bindContainerListener(listeners.container)
        giftView.setListeners(listeners.cell)
    }

    override fun bind(item: ChatMessageDataUiModel) {
        super.bind(item)
        giftView.setState(
            (item.messageConfig as MessageConfigWrapperUiModel.Gift).config.copy(
                giftMessage = meeraSpanListenersMapper.mapSpannableListeners(item.messageData)
            )
        )
    }
}
