package com.numplates.nomera3.modules.search.ui.adapter.recent

import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hannesdorfmann.adapterdelegates4.ListDelegationAdapter
import com.hannesdorfmann.adapterdelegates4.dsl.adapterDelegate
import com.meera.core.extensions.setThrottledClickListener
import com.meera.uikit.widgets.buttons.UiKitButton
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.search.ui.entity.SearchItem

/**
 * Блок "Недавнее"
 */
fun searchRecentBlockAdapterDelegate(
    selectItemCallback: (SearchItem.RecentBlock.RecentBaseItem) -> Unit,
    clearCallback: (SearchItem.RecentBlock) -> Unit
) = adapterDelegate<SearchItem.RecentBlock, SearchItem>(R.layout.search_recent_block) {

    val searchRecentRecycler: RecyclerView = findViewById(R.id.search_recent_recycler)
    val clearButton: TextView = findViewById(R.id.clear_button)

    searchRecentRecycler.layoutManager =
        LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)

    searchRecentRecycler.adapter =
        ListDelegationAdapter(
            searchRecentUserAdapterDelegate(selectItemCallback),
            searchRecentGroupAdapterDelegate(selectItemCallback),
        )

    clearButton.setOnClickListener { clearButton ->
        clearCallback(item)
    }

    bind {
        (searchRecentRecycler.adapter as ListDelegationAdapter<*>).apply {
            items = item.items
            notifyDataSetChanged()
        }
    }
}

fun meeraSearchRecentBlockAdapterDelegate(
    selectItemCallback: (SearchItem.RecentBlock.RecentBaseItem) -> Unit,
    clearCallback: (SearchItem.RecentBlock) -> Unit
) = adapterDelegate<SearchItem.RecentBlock, SearchItem>(R.layout.meera_item_recent_users) {

    val rvSearchRecent: RecyclerView = findViewById(R.id.rv_search_recent)
    val btnClear: UiKitButton = findViewById(R.id.btn_clear_recent)

    rvSearchRecent.layoutManager =
        LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)

    rvSearchRecent.adapter =
        ListDelegationAdapter(
            searchRecentUserAdapterDelegate(selectItemCallback),
            searchRecentGroupAdapterDelegate(selectItemCallback),
        )

    btnClear.setThrottledClickListener { clearCallback(item) }

    bind {
        (rvSearchRecent.adapter as ListDelegationAdapter<*>).apply {
            items = item.items
            notifyDataSetChanged()
        }
    }
}
