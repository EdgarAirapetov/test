package com.numplates.nomera3.modules.tags.ui.viewholder

import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.tags.ui.entity.SuggestedTagListUIModel
import com.numplates.nomera3.modules.tags.ui.entity.SuggestedTagListUIModel.HashtagUIModel
import com.meera.core.extensions.string

class HashtagViewHolder(val view: View) : RecyclerView.ViewHolder(view) {

    private val root: LinearLayout = view.findViewById(R.id.hashtag_item_root)
    private val hashtagName: TextView = view.findViewById(R.id.hashtag_item_name)
    private val hashtagCount: TextView = view.findViewById(R.id.hashtag_item_count)

    fun bind(itemData: HashtagUIModel?, onItemClick: ((SuggestedTagListUIModel) -> Unit)?) {
        itemData?.also { model: HashtagUIModel ->
            hashtagName.text = model.name.addHashtagSign()
            if (model.count > 0) {
                hashtagCount.text = itemView.context.string(R.string.hashtag_view_holder_post_count_text) + model.count
            } else hashtagCount.text = ""

            root.setOnClickListener { onItemClick?.invoke(model) }
        }
    }

    fun unbind() {
        root.setOnClickListener(null)
    }


    private fun String.addHashtagSign(): String {
        return if (!this.contains("#")) "#${this}" else this
    }
}
