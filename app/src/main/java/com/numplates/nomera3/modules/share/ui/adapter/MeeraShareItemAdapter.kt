package com.numplates.nomera3.modules.share.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.numplates.nomera3.databinding.MeeraShareItemBinding
import com.numplates.nomera3.modules.share.ui.ShareItemsCallback
import com.numplates.nomera3.modules.share.ui.entity.UIShareItem
import com.numplates.nomera3.modules.share.ui.holder.MeeraShareItemHolder

class MeeraShareItemAdapter(
    private val shareItemCallback: ShareItemsCallback
) : ListAdapter<UIShareItem, MeeraShareItemHolder>(DIFF_CALLBACK) {

    override fun getItemId(position: Int): Long {
        return currentList[position].id.filter { it.isDigit() }.toLong()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MeeraShareItemHolder {
        val binding = MeeraShareItemBinding.inflate(LayoutInflater.from(parent.context))
        return MeeraShareItemHolder(shareItemCallback, binding)
    }

    override fun onBindViewHolder(holder: MeeraShareItemHolder, position: Int) {
        val lastPosition = currentList.lastIndex == position
        holder.bind(currentList[position], lastPosition)
    }
}

private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<UIShareItem>() {
    override fun areContentsTheSame(oldItem: UIShareItem, newItem: UIShareItem): Boolean {
        return oldItem == newItem
    }

    override fun areItemsTheSame(oldItem: UIShareItem, newItem: UIShareItem): Boolean {
        return oldItem.id == newItem.id
    }
}
