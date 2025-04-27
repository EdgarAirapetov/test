package com.numplates.nomera3.modules.tags.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.tags.ui.entity.UITagEntity
import com.numplates.nomera3.modules.tags.ui.viewholder.TagViewHolder

class TagsListAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val items = mutableListOf<UITagEntity>()

    var onTagClick: (UITagEntity) -> Unit = {}
    var isDarkColoredBackground = false

    fun addItems(data: List<UITagEntity>) {
        items.addAll(data)
        notifyItemInserted(items.size - 1)
    }

    fun addItemAndClear(data: List<UITagEntity>) {
        val itemCount = items.size
        items.clear()
        notifyItemRangeRemoved(0, itemCount)
        items.addAll(data)
        notifyItemRangeInserted(0, data.size)
        notifyDataSetChanged()
    }

    fun clearAdapter() {
        items.clear()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.search_tag_item, parent, false)
        return TagViewHolder(v)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is TagViewHolder -> holder.bind(items[position], isDarkColoredBackground, onTagClick)
        }
    }

    override fun getItemCount(): Int = items.size

}
