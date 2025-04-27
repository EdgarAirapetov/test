package com.numplates.nomera3.modules.search.ui.util

import androidx.recyclerview.widget.DiffUtil
import com.numplates.nomera3.modules.search.ui.entity.SearchItem

class SearchResultDiffCallback : DiffUtil.ItemCallback<SearchItem>() {
    override fun areItemsTheSame(oldItem: SearchItem, newItem: SearchItem): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: SearchItem, newItem: SearchItem): Boolean {
        return oldItem == newItem
    }
}