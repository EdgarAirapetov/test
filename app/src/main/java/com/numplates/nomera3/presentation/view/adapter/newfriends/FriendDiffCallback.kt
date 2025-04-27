package com.numplates.nomera3.presentation.view.adapter.newfriends

import androidx.recyclerview.widget.DiffUtil

class FriendDiffCallback(private val oldList: List<FriendModel>, private val newList: List<FriendModel>) :
    DiffUtil.Callback() {


    override fun getOldListSize(): Int = oldList.size
    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldItem = oldList.getOrNull(oldItemPosition)
        val newItem = newList.getOrNull(newItemPosition)
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldItem = oldList.getOrNull(oldItemPosition)
        val newItem = newList.getOrNull(newItemPosition)
        return oldItem == newItem
    }
}
