package com.numplates.nomera3.modules.search.ui.adapter.recent

import android.widget.TextView
import com.hannesdorfmann.adapterdelegates4.dsl.adapterDelegate
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.search.ui.entity.SearchItem
import com.meera.core.extensions.clickAnimate
import com.numplates.nomera3.presentation.view.widgets.VipView

/**
 * Элемент блока "Недавнее" – "Пользователь"
 */
fun searchRecentUserAdapterDelegate(selectCallback: (SearchItem.RecentBlock.RecentBaseItem.RecentUser) -> Unit) =
    adapterDelegate<SearchItem.RecentBlock.RecentBaseItem.RecentUser, SearchItem.RecentBlock.RecentBaseItem>(
        R.layout.search_recent_user_item
    ) {
        val searchRecentItemAvatarView: VipView =
            findViewById(R.id.search_recent_item_avatar_view)
        val searchRecentItemNameText: TextView = findViewById(R.id.search_recent_item_name_text)

        itemView.setOnClickListener {
            searchRecentItemAvatarView.clickAnimate()
            selectCallback(item)
        }

        bind {
            searchRecentItemNameText.text = item.name
            searchRecentItemAvatarView.setUp(
                context = context,
                avatarLink = item.image,
                accountType = item.accountType.value,
                frameColor = item.accountColor,
                hasMoments = item.hasMoments,
                hasNewMoments = item.hasNewMoments
            )
        }
    }
