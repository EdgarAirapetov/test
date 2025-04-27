package com.numplates.nomera3.modules.tags.ui.viewholder

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.tags.ui.entity.UITagEntity
import com.numplates.nomera3.modules.tags.ui.viewmodel.TagMenuViewModelNew.TagListItem
import com.numplates.nomera3.modules.tags.ui.viewmodel.TagMenuViewModelNew.TagListItem.HashtagItem
import com.numplates.nomera3.modules.tags.ui.viewmodel.TagMenuViewModelNew.TagListItem.UniqueNameItem

class EmptyViewHolder(val view: View) : RecyclerView.ViewHolder(view) {

    private val userName: TextView = view.findViewById(R.id.tv_user_name_tag)
    private val uniqueName: TextView = view.findViewById(R.id.tv_tag_unique_name)
    private val userAvatar: ImageView = view.findViewById(R.id.iv_user_avatar_tag)
    private val tagItemContainer: ConstraintLayout = view.findViewById(R.id.cl_tag_item_container)

    fun bind(itemData: TagListItem, onItemClick: (TagListItem) -> Unit) {
        when (itemData) {
            is HashtagItem -> bindAsHashtagItem(itemData.data)
            is UniqueNameItem -> bindAsUniqueNameItem(itemData.data)
        }

        tagItemContainer.setOnClickListener {
            onItemClick(itemData)
        }
    }

    private fun bindAsUniqueNameItem(itemData: UITagEntity) {
        userName.text = itemData.userName ?: ""
        uniqueName.text = "@" + itemData.uniqueName

        itemData.image?.let {
            val options = RequestOptions()
                    .centerCrop()
                    .apply(RequestOptions.circleCropTransform())
                    .placeholder(R.drawable.fill_8_round)
                    .error(R.drawable.fill_8_round)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .priority(Priority.HIGH)

            Glide.with(userAvatar.context)
                    .load(it)
                    .apply(options)
                    .into(userAvatar)
        }
    }

    private fun bindAsHashtagItem(itemData: UITagEntity) {
        userName.text = itemData.userName ?: ""
        uniqueName.text = "@" + itemData.uniqueName

        itemData.image?.let {
            val options = RequestOptions()
                    .centerCrop()
                    .apply(RequestOptions.circleCropTransform())
                    .placeholder(R.drawable.fill_8_round)
                    .error(R.drawable.fill_8_round)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .priority(Priority.HIGH)

            Glide.with(userAvatar.context)
                    .load(it)
                    .apply(options)
                    .into(userAvatar)
        }
    }
}
