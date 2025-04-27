package com.numplates.nomera3.modules.reaction.ui.util

import androidx.recyclerview.widget.DiffUtil
import com.numplates.nomera3.modules.reaction.data.net.ReactionEntity

class ReactionDiffCallback(
    val newItems: List<ReactionEntity>,
    val oldItems: List<ReactionEntity>
) : DiffUtil.Callback() {

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldItems[oldItemPosition].reactionType == newItems[newItemPosition].reactionType
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldItems[oldItemPosition] == newItems[newItemPosition]
    }

    override fun getOldListSize(): Int {
        return oldItems.size
    }

    override fun getNewListSize(): Int {
        return newItems.size
    }
}