package com.numplates.nomera3.modules.hashtag.ui.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.numplates.nomera3.modules.hashtag.ui.viewholder.HashtagInfoViewHolder
import com.meera.core.extensions.empty

class HashtagAdapter: RecyclerView.Adapter<HashtagInfoViewHolder>() {

    private var text: String = String.empty()

    fun setTotalPostsCountText(text: String) {
        this.text = text
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HashtagInfoViewHolder {
        return HashtagInfoViewHolder(parent)
    }

    override fun onBindViewHolder(holder: HashtagInfoViewHolder, position: Int) {
        holder.bind(text)
    }

    override fun getItemCount() = 1

}