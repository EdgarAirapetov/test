package com.numplates.nomera3.modules.newroads.ui.adapter

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.newroads.ui.entity.QuickAnswerEntity

class QuickAnswerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private val tvEmoji: TextView = itemView.findViewById(R.id.tv_emoji_item)

    fun bind(data: QuickAnswerEntity, clickListener: (String, String) -> Unit) {
        tvEmoji.text = data.emoji
        tvEmoji.setOnClickListener { clickListener(data.emoji, data.name) }
    }

}