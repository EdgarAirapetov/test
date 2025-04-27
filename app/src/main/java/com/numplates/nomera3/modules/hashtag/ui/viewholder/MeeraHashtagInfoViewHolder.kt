package com.numplates.nomera3.modules.hashtag.ui.viewholder

import androidx.recyclerview.widget.RecyclerView
import com.numplates.nomera3.databinding.MeeraItemHashtagInfoBinding

class MeeraHashtagInfoViewHolder(
    private val binding: MeeraItemHashtagInfoBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(postsQuantityText: String) {
        binding.tvPostsWithHashtag.text = postsQuantityText
    }
}
