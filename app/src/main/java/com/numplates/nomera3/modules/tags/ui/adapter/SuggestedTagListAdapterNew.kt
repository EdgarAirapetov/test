package com.numplates.nomera3.modules.tags.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.tags.ui.entity.SuggestedTagListUIModel
import com.numplates.nomera3.modules.tags.ui.entity.SuggestedTagListUIModel.HashtagUIModel
import com.numplates.nomera3.modules.tags.ui.entity.SuggestedTagListUIModel.UniqueNameUIModel
import com.numplates.nomera3.modules.tags.ui.viewholder.EmptyViewHolder
import com.numplates.nomera3.modules.tags.ui.viewholder.HashtagViewHolder
import com.numplates.nomera3.modules.tags.ui.viewholder.UniqueNameViewHolder

class SuggestedTagListAdapterNew(
        private val tagList: MutableList<SuggestedTagListUIModel> = mutableListOf()
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var itemClickListener: ((SuggestedTagListUIModel) -> Unit)? = null

    fun setOnItemClickListener(listener: ((SuggestedTagListUIModel) -> Unit)?) {
        itemClickListener = listener
    }

    override fun getItemViewType(position: Int): Int {
        return when (tagList[position]) {
            is HashtagUIModel -> R.layout.tag_list_adapter_item_hashtag
            is UniqueNameUIModel -> R.layout.tag_list_adapter_item_uniquename
            else -> R.layout.tag_list_adapter_item_empty
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater
                .from(parent.context)
                .inflate(viewType, parent, false)

        return when (viewType) {
            R.layout.tag_list_adapter_item_hashtag -> HashtagViewHolder(view)
            R.layout.tag_list_adapter_item_uniquename -> UniqueNameViewHolder(view)
            else -> EmptyViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = tagList[position]
        when (holder) {
            is HashtagViewHolder -> holder.bind(item as? HashtagUIModel, itemClickListener)
            is UniqueNameViewHolder -> holder.bind(item as? UniqueNameUIModel, itemClickListener)
        }
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        if (holder is HashtagViewHolder) {
            holder.unbind()
        }

        if (holder is UniqueNameViewHolder) {
            holder.unbind()
        }

        super.onViewRecycled(holder)
    }

    override fun getItemCount(): Int = tagList.size

    fun setTagList(newList: List<SuggestedTagListUIModel>) {
        notifyItemRangeRemoved(0, tagList.size)
        tagList.clear()
        tagList.addAll(newList)
        notifyItemRangeInserted(0, tagList.size)
        notifyDataSetChanged()
    }

    fun clearAdapter() {
        tagList.clear()
        notifyDataSetChanged()
    }
}
