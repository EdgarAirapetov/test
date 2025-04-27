package com.numplates.nomera3.modules.search.ui.adapter.recent

import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.hannesdorfmann.adapterdelegates4.dsl.adapterDelegate
import com.meera.core.extensions.clickAnimate
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.search.ui.entity.SearchItem

/**
 * Элемент блока "Недавнее" – "Сообщества"
 */
fun searchRecentGroupAdapterDelegate(selectCallback: (SearchItem.RecentBlock.RecentBaseItem) -> Unit) =
    adapterDelegate<SearchItem.RecentBlock.RecentBaseItem.RecentGroup, SearchItem.RecentBlock.RecentBaseItem>(
        R.layout.search_recent_group_item
    ) {
        val avatarView: ImageView = findViewById(R.id.avatar_view)
        val searchRecentItemNameText: TextView = findViewById(R.id.search_recent_item_name_text)

        itemView.setOnClickListener {
            avatarView.clickAnimate()
            selectCallback(item)
        }

        bind {
            searchRecentItemNameText.text = item.name

            if (item.image.isNullOrBlank()) {
                avatarView.scaleType = ImageView.ScaleType.CENTER
                avatarView.setBackgroundResource(R.drawable.meera_bg_community_image_placeholder)
                avatarView.setImageResource(R.drawable.ic_outlined_photo_l)
            } else {
                avatarView.scaleType = ImageView.ScaleType.FIT_CENTER
                Glide.with(context)
                    .load(item.image)
                    .apply(RequestOptions.circleCropTransform())
                    .into(avatarView)
            }
        }
    }
