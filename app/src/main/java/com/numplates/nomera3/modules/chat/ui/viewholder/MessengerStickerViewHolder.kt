package com.numplates.nomera3.modules.chat.ui.viewholder

import android.view.View
import com.meera.uikit.widgets.chat.highlight.TintImageHighlighter
import com.meera.uikit.widgets.chat.highlight.TintLottieHighlighter
import com.meera.uikit.widgets.chat.sticker.UiKitStickersView
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.chat.helpers.replymessage.ISwipeableHolder
import com.numplates.nomera3.modules.chat.ui.highlight.Highlightable
import com.numplates.nomera3.modules.chat.ui.listeners.MeeraMessagesListenersMapper
import com.numplates.nomera3.modules.chat.ui.model.ChatMessageDataUiModel
import com.numplates.nomera3.modules.chat.ui.model.MessageConfigWrapperUiModel
import com.numplates.nomera3.modules.chat.ui.model.MessageWrapperUiListeners

class MessengerStickerViewHolder(
    itemView: View,
    listenersMapper: MeeraMessagesListenersMapper,
) : BaseMessageViewHolder(itemView), ISwipeableHolder, Highlightable {

    private val stickerView = itemView as UiKitStickersView

    init {
        val listeners = listenersMapper.mapStickerListeners(::getMessage) as MessageWrapperUiListeners.StickerListeners
        bindContainerListener(listeners.container)
        stickerView.setListeners(listeners.cell)
    }

    override fun getSwipeContainer(): View = itemView

    override fun highlight() {
        stickerView.highlight(
            imageHighlighter = TintImageHighlighter(tintColor = itemView.context.getColor(R.color.uiKitColorFadePrimary40)),
            lottieHighlighter = TintLottieHighlighter(tintColor = itemView.context.getColor(R.color.uiKitColorFadePrimary40))
        )
    }

    override fun bind(item: ChatMessageDataUiModel) {
        super.bind(item)
        stickerView.setConfig((item.messageConfig as MessageConfigWrapperUiModel.Sticker).config)
    }
}
