package com.numplates.nomera3.modules.gifservice.ui.adapter

import android.graphics.Color
import android.view.View
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.meera.core.extensions.dp
import com.meera.core.extensions.setMargins
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.gifservice.data.entity.GiphyEmoji
import com.numplates.nomera3.modules.gifservice.ui.entity.GifEmojiEntity
import com.meera.core.extensions.loadGlide


private const val FIRST_ITEM_LEFT_MARGIN = 16

class GifEmojiImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    private val container: ConstraintLayout = itemView.findViewById(R.id.emoji_image_container)
    private val ivImage: ImageView = itemView.findViewById(R.id.iv_emoji_item)

    fun bind(data: GifEmojiEntity, clickListener: (GifEmojiEntity, Int) -> Unit, position: Int) {
        ivImage.loadGlide(data.emojiDrawableRes)
        if (data.isSelected) {
            container.setBackgroundResource(R.drawable.emoji_selected_rectangle)
        } else {
            container.setBackgroundColor(Color.TRANSPARENT)
        }
        if (data.emojiQuery == GiphyEmoji.TOP) {
            itemView.setMargins(start = FIRST_ITEM_LEFT_MARGIN.dp)
        }
        itemView.setOnClickListener { clickListener(data, position) }
    }

}