package com.numplates.nomera3.modules.search.filters.ui.adapter

import android.content.Context
import android.graphics.Canvas
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import com.meera.core.extensions.dp
import com.numplates.nomera3.R

class MeeraFilterCitiesDecoration : ItemDecoration() {

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        val childCount = parent.childCount
        val dividerDrawable = parent.context.getDivider() ?: return
        for (i in 0..childCount - 2) {
            val child: View = parent.getChildAt(i)
            val params = child.layoutParams as RecyclerView.LayoutParams
            val dividerTop: Int = child.bottom + params.bottomMargin
            val dividerBottom: Int = dividerTop + dividerDrawable.intrinsicHeight
            val dividerLeft = parent.paddingLeft + 16.dp
            val dividerRight = parent.width - parent.paddingLeft - 16.dp
            dividerDrawable.setBounds(dividerLeft, dividerTop, dividerRight, dividerBottom)
            dividerDrawable.draw(c)
        }
    }

    private fun Context.getDivider() =
        ContextCompat.getDrawable(this, R.drawable.friends_select_divider)

}
