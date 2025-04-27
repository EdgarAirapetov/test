package com.numplates.nomera3.modules.gifservice.ui.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.gifservice.ui.entity.GifEmojiEntity
import com.numplates.nomera3.modules.gifservice.ui.entity.GifEmojiItemType
import com.meera.core.extensions.inflate
import java.lang.RuntimeException


class GifEmojiAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val collection: MutableList<GifEmojiEntity> = mutableListOf()

    internal var clickListener: (GifEmojiEntity, Int) -> Unit = { _, _ -> }

    fun addItems(items: List<GifEmojiEntity>) {
        collection.clear()
        collection.addAll(items)
        notifyDataSetChanged()
    }

    override fun getItemCount() = collection.size

    override fun getItemViewType(position: Int): Int {
        return collection[position].itemType.key
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when(viewType){
            GifEmojiItemType.TEXT.key ->
                GifEmojiTextViewHolder(parent.inflate(R.layout.gif_emoji_text_item))
            GifEmojiItemType.IMAGE.key ->
                GifEmojiImageViewHolder(parent.inflate(R.layout.gif_emoji_image_item))
            else -> throw RuntimeException("Emoji type not defined")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(collection[position].itemType){
            GifEmojiItemType.TEXT -> (holder as GifEmojiTextViewHolder)
                    .bind(collection[position], clickListener, position)
            GifEmojiItemType.IMAGE -> (holder as GifEmojiImageViewHolder)
                    .bind(collection[position], clickListener, position)
        }
    }

}