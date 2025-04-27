package com.numplates.nomera3.modules.chat.ui.viewholder

import android.view.View
import androidx.annotation.CallSuper
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.meera.uikit.widgets.chat.container.UiKitMessagesContainerListeners
import com.meera.uikit.widgets.chat.container.UiKitMessagesContainerView
import com.numplates.nomera3.modules.chat.ui.model.ChatMessageDataUiModel

abstract class BaseMessageViewHolder(
    itemView: View,
) : RecyclerView.ViewHolder(wrapBubble(itemView)) {

    private val wrappedBubble: UiKitMessagesContainerView
        get() = itemView as UiKitMessagesContainerView

    @CallSuper
    open fun bind(item: ChatMessageDataUiModel) {
        wrappedBubble.setConfig(item.containerConfig)
    }

    protected fun bindContainerListener(listener: UiKitMessagesContainerListeners?) {
        wrappedBubble.setListeners(listener)
    }

    protected fun getMessage(): ChatMessageDataUiModel? {
        val adapter = bindingAdapter as? ListAdapter<*, *>
        val item = adapter?.currentList?.get(bindingAdapterPosition) as? ChatMessageDataUiModel
        return item
    }

    companion object {
        private fun wrapBubble(itemView: View): UiKitMessagesContainerView {
            return UiKitMessagesContainerView(
                context = itemView.context,
                attrs = null
            ).apply {
                inflateBubble(itemView)
            }
        }
    }
}
