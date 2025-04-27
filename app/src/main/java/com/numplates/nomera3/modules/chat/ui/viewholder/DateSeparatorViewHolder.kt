package com.numplates.nomera3.modules.chat.ui.viewholder

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.meera.core.utils.timeAgoChat
import com.numplates.nomera3.databinding.MeeraChatItemServiceMessageBinding
import com.numplates.nomera3.modules.chat.ui.model.ChatMessageDataUiModel

class DateSeparatorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private val binding = MeeraChatItemServiceMessageBinding.bind(itemView)

    fun bind(item: ChatMessageDataUiModel) {
        binding.tvServiceMessage.text = timeAgoChat(itemView.context, item.messageData.createdAt)
    }
}
