package com.numplates.nomera3.modules.chat.ui.viewholder

import android.annotation.SuppressLint
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.numplates.nomera3.databinding.ItemChatEmptyViewBinding
import com.numplates.nomera3.modules.chat.ui.model.ChatMessageDataUiModel

class EmptyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private val binding = ItemChatEmptyViewBinding.bind(itemView)

    @SuppressLint("SetTextI18n")
    fun bind(item: ChatMessageDataUiModel) {
        binding.text.text = "ITEM_TYPE: ${item.messageData.messageType}"
    }
}
