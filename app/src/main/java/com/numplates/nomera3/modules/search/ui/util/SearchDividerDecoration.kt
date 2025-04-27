package com.numplates.nomera3.modules.search.ui.util

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.hannesdorfmann.adapterdelegates4.ListDelegationAdapter
import com.meera.core.adapters.CustomDividerItemDecoration
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.search.ui.entity.SearchItem
import com.meera.core.extensions.dp

class SearchDividerDecoration(
    context: Context,
    drawableId: Int,
) : CustomDividerItemDecoration(context, DividerItemDecoration.VERTICAL, drawableId) {

    override fun validateVerticalDivider(child: View, parent: RecyclerView): Boolean {
        val current = parent.getCurrentItem(child) ?: return false

        return when (current) {
            is SearchItem.Title -> {
                false
            }
            else -> {
                true
            }
        }
    }

    override fun getHorizontalPadding(): Int {
        return 16.dp
    }

    private fun RecyclerView.getCurrentItem(child: View): SearchItem? {
        val adapter = (adapter as ListDelegationAdapter<List<SearchItem>>)
        val position = getChildAdapterPosition(child)

        return adapter.items?.getOrNull(position)
    }

    companion object {
        fun build(context: Context): CustomDividerItemDecoration {
            return SearchDividerDecoration(
                context,
                R.drawable.drawable_divider_decoration
            )
        }
    }
}
