package com.numplates.nomera3.modules.tags.ui.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.meera.core.adapters.baserecycleradapter.toBinding
import com.numplates.nomera3.modules.tags.ui.entity.UITagEntity
import com.numplates.nomera3.modules.tags.ui.viewholder.MeeraTagViewHolder

class MeeraTagsListAdapter(private val isDarkMode: Boolean) : ListAdapter<UITagEntity, MeeraTagViewHolder>(DIFF_CALLBACK) {

    private val items = mutableListOf<UITagEntity>()

    var onTagClick: (UITagEntity) -> Unit = {}

    fun clearAdapter() {
        items.clear()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MeeraTagViewHolder {
        return MeeraTagViewHolder(parent.toBinding(), isDarkMode)
    }

    override fun onBindViewHolder(holder: MeeraTagViewHolder, position: Int) {
        holder.bind(currentList[position], onTagClick)
    }
}
private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<UITagEntity>() {
    override fun areContentsTheSame(oldItem: UITagEntity, newItem: UITagEntity): Boolean {
        return oldItem == newItem
    }

    override fun areItemsTheSame(oldItem: UITagEntity, newItem: UITagEntity): Boolean {
        return oldItem == newItem
    }
}
