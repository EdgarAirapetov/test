package com.numplates.nomera3.modules.peoples.ui.content.decorator

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.meera.core.extensions.dp
import com.numplates.nomera3.R
import com.numplates.nomera3.modules.peoples.ui.content.adapter.PeoplesContentType
import com.numplates.nomera3.modules.peoples.ui.utils.OffsetProvider

private const val DEFAULT_HORIZONTAL_PADDING = 16
private const val USER_SEARCH_RESULT_PADDING = 91
private const val FIRST_TOP_VERTICAL_PADDING = 16

private const val FIRST_ADAPTER_POSITION = 0
private const val LABEL_DEFAULT_TOP_MARGIN = 16
private const val LABEL_DEFAULT_BOTTOM_MARGIN = 6
private const val LABEL_FIRST_TOP_MARGIN = 24

class PeoplesContentDecorator : RecyclerView.ItemDecoration() {

    override fun onDrawOver(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        val childCount = parent.childCount
        val dividerDrawable = parent.context.getFriendsSelectDivider() ?: return
        for (i in 0..childCount - 2) {
            val child: View = parent.getChildAt(i)
            val viewHolder = parent.getChildViewHolder(child)
            val params = child.layoutParams as RecyclerView.LayoutParams
            val dividerTop: Int = child.bottom + params.bottomMargin
            val dividerBottom: Int = dividerTop + dividerDrawable.intrinsicHeight
            when (parent.adapter?.getItemViewType(viewHolder.absoluteAdapterPosition)) {
                PeoplesContentType.SEARCH_RESULT_SHIMMER_TYPE.ordinal,
                PeoplesContentType.USER_SEARCH_RESULT.ordinal -> {
                    val dividerLeft = parent.paddingLeft + USER_SEARCH_RESULT_PADDING.dp
                    val dividerRight = parent.width - parent.paddingLeft
                    dividerDrawable.setBounds(dividerLeft, dividerTop, dividerRight, dividerBottom)
                    dividerDrawable.draw(canvas)
                }
            }
        }
    }

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val position = parent.getChildAdapterPosition(view)
        when (parent.adapter?.getItemViewType(position)) {
            PeoplesContentType.FIND_FRIENDS_TYPE.ordinal -> {
                val holder = parent.getChildViewHolder(view)
                if (holder !is OffsetProvider) return
                outRect.top = holder.provide()
            }
            PeoplesContentType.CONTACT_SYNC_TYPE.ordinal -> {
                with(outRect) {
                    left = DEFAULT_HORIZONTAL_PADDING.dp
                    right = DEFAULT_HORIZONTAL_PADDING.dp
                    top = FIRST_TOP_VERTICAL_PADDING.dp
                }
            }
            PeoplesContentType.HEADER_TYPE.ordinal -> {
                with(outRect) {
                    top = if (parent.getChildAdapterPosition(view) == FIRST_ADAPTER_POSITION) {
                        LABEL_FIRST_TOP_MARGIN.dp
                    } else {
                        LABEL_DEFAULT_TOP_MARGIN.dp
                    }
                    left = DEFAULT_HORIZONTAL_PADDING.dp
                    bottom = LABEL_DEFAULT_BOTTOM_MARGIN.dp
                }
            }
            PeoplesContentType.BLOGGERS_PLACEHOLDER.ordinal -> {
                with(outRect) {
                    left = DEFAULT_HORIZONTAL_PADDING.dp
                    right = DEFAULT_HORIZONTAL_PADDING.dp
                    top = FIRST_TOP_VERTICAL_PADDING.dp
                }
            }
        }
    }

    private fun Context.getFriendsSelectDivider() =
        ContextCompat.getDrawable(this, R.drawable.friends_select_divider)
}
