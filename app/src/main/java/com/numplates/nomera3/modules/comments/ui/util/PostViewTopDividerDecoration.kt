package com.numplates.nomera3.modules.comments.ui.util

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.meera.core.adapters.CustomDividerItemDecoration
import com.numplates.nomera3.R
import com.meera.core.extensions.dp

private const val TOP_POSITION = 0

class PostViewTopDividerDecoration(
    context: Context,
    drawableId: Int,
) : CustomDividerItemDecoration(context, DividerItemDecoration.VERTICAL, drawableId) {

    override fun validateVerticalDivider(child: View, parent: RecyclerView): Boolean {
        return parent.getChildAdapterPosition(child) == TOP_POSITION
    }

    override fun getHorizontalPadding(): Int {
        return 0.dp
    }

    companion object {
        fun build(context: Context): CustomDividerItemDecoration {
            return PostViewTopDividerDecoration(
                context,
                R.drawable.post_view_divider
            )
        }
    }
}
