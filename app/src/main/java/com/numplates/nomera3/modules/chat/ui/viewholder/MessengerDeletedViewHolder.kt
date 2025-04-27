package com.numplates.nomera3.modules.chat.ui.viewholder

import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.meera.core.extensions.toFloat
import com.numplates.nomera3.databinding.MeeraChatItemDeletedBinding
import com.numplates.nomera3.modules.chat.ui.model.ChatMessageDataUiModel
import com.numplates.nomera3.modules.chat.ui.model.MessageConfigWrapperUiModel

class MessengerDeletedViewHolder(
    itemView: View,
) : BaseMessageViewHolder(itemView) {

    private val binding = MeeraChatItemDeletedBinding.bind(itemView)

    override fun bind(item: ChatMessageDataUiModel) {
        super.bind(item)
        val deleted = item.messageConfig as MessageConfigWrapperUiModel.Deleted
        setBiasByType(deleted.isMy)
        binding.msvMessageDeleteStatus.setConfig(deleted.statusConfig)
    }

    private fun setBiasByType(isMy: Boolean) {
        val clParams = binding.vgMessageDeleteContainer.layoutParams as ConstraintLayout.LayoutParams
        clParams.horizontalBias = isMy.toFloat()
        binding.vgMessageDeleteContainer.layoutParams = clParams
    }
}
