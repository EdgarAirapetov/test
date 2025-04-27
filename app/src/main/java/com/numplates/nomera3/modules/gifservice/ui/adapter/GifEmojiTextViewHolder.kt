package com.numplates.nomera3.modules.gifservice.ui.adapter

import android.graphics.Color
import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.gifservice.ui.entity.GifEmojiEntity

class GifEmojiTextViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

    private val container: ConstraintLayout = itemView.findViewById(R.id.emoji_text_container)
    private val tvEmoji: TextView = itemView.findViewById(R.id.tv_emoji_item)

    fun bind(data: GifEmojiEntity, clickListener: (GifEmojiEntity, Int) -> Unit, position: Int) {
        tvEmoji.text = data.emojiText
        if (data.isSelected) {
            container.setBackgroundResource(R.drawable.emoji_selected_rectangle)
        } else {
            container.setBackgroundColor(Color.TRANSPARENT)
        }
        itemView.setOnClickListener { clickListener(data, position) }
    }

}