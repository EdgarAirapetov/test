package com.numplates.nomera3.modules.hashtag.ui.viewholder

import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.meera.core.extensions.inflate
import com.numplates.nomera3.R


class HashtagInfoViewHolder(
    parent: ViewGroup
) : RecyclerView.ViewHolder(parent.inflate(R.layout.item_hashtag_info)) {

    private val postsQuantity = itemView.findViewById<TextView>(R.id.tv_posts_with_hashtag)

    fun bind(postsQuantityText: String) {
        postsQuantity?.text = postsQuantityText
    }
}
