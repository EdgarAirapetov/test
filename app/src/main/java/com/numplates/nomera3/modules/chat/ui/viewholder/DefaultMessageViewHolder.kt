package com.numplates.nomera3.modules.chat.ui.viewholder

import android.view.View
import com.meera.core.extensions.gone
import com.meera.core.extensions.visible
import com.meera.uikit.widgets.chat.emoji.UiKitEmojiConfig
import com.meera.uikit.widgets.chat.highlight.EmojiHighlighter
import com.meera.uikit.widgets.chat.highlight.TintBackgroundHighlighter
import com.meera.uikit.widgets.chat.regular.UiKitRegularConfig
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraChatItemDefaultBinding
import com.numplates.nomera3.modules.chat.helpers.replymessage.ISwipeableHolder
import com.numplates.nomera3.modules.chat.ui.highlight.Highlightable
import com.numplates.nomera3.modules.chat.ui.listeners.MeeraMessagesListenersMapper
import com.numplates.nomera3.modules.chat.ui.listeners.MeeraSpanListenersMapper
import com.numplates.nomera3.modules.chat.ui.model.ChatMessageDataUiModel
import com.numplates.nomera3.modules.chat.ui.model.MessageConfigWrapperUiModel
import com.numplates.nomera3.modules.chat.ui.model.MessageUiModel
import com.numplates.nomera3.modules.chat.ui.model.MessageWrapperUiListeners

class DefaultMessageViewHolder(
    itemView: View,
    listenersMapper: MeeraMessagesListenersMapper,
    private val meeraSpanListenersMapper: MeeraSpanListenersMapper,
) : BaseMessageViewHolder(itemView), ISwipeableHolder, Highlightable {

    private val binding = MeeraChatItemDefaultBinding.bind(itemView)

    init {
        val textListeners =
            (listenersMapper.mapTextListeners(::getMessage) as? MessageWrapperUiListeners.RegularListeners)
        val emojiListeners =
            (listenersMapper.mapEmojiListener(::getMessage) as? MessageWrapperUiListeners.EmojiListeners)
        bindContainerListener(textListeners?.container ?: emojiListeners?.container)
        binding.mcidDefaultBubble.setListeners(textListeners?.cell)
        binding.mcidEmojiBubble.setListeners(emojiListeners?.cell)
    }

    override fun getSwipeContainer(): View = itemView

    override fun highlight() {
        binding.mcidEmojiBubble.highlight(EmojiHighlighter(color = itemView.context.getColor(R.color.uiKitColorFadePrimary40)))
        binding.mcidDefaultBubble.highlight(TintBackgroundHighlighter(tintColor = itemView.context.getColor(R.color.uiKitColorGradientMiddle)))
    }

    override fun bind(item: ChatMessageDataUiModel) {
        super.bind(item)
        when (item.messageConfig) {
            is MessageConfigWrapperUiModel.Default -> bindDefault(item.messageConfig.config, item.messageData)
            is MessageConfigWrapperUiModel.Emoji -> bindEmoji(item.messageConfig.config)
            else -> bindEmpty()
        }
    }

    private fun bindDefault(config: UiKitRegularConfig, data: MessageUiModel) {
        binding.mcidEmojiBubble.gone()
        binding.mcidDefaultBubble.visible()
        binding.mcidDefaultBubble.setConfig(config.copy(message = meeraSpanListenersMapper.mapSpannableListeners(data)))
    }

    private fun bindEmoji(config: UiKitEmojiConfig) {
        binding.mcidDefaultBubble.gone()
        binding.mcidEmojiBubble.visible()
        binding.mcidEmojiBubble.setConfig(config)
    }

    private fun bindEmpty() {
        binding.mcidDefaultBubble.gone()
        binding.mcidEmojiBubble.gone()
    }
}
