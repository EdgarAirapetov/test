package com.numplates.nomera3.presentation.view.utils.sharedialog

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import androidx.core.view.children
import androidx.recyclerview.widget.RecyclerView
import com.numplates.nomera3.R
import com.meera.core.extensions.dpToPx
import com.meera.core.extensions.getDrawableCompat

class ShareDividerItemDecorator(context: Context) : RecyclerView.ItemDecoration() {

    private val divider = context.getDrawableCompat(R.drawable.drawable_divider_decoration_share)

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        divider?.let {
            val horizontalMargin = dpToPx(20)
            val left = parent.paddingLeft + horizontalMargin
            val right = parent.width - parent.paddingRight - horizontalMargin
            parent.children.forEach { view ->
                val childPosition = parent.getChildAdapterPosition(view)
                val itemCount = parent.adapter?.itemCount ?: 0
                if (childPosition < itemCount - 1) {
                    val params = view.layoutParams as RecyclerView.LayoutParams

                    val top = view.bottom + params.bottomMargin
                    val bottom = top + divider.intrinsicHeight

                    divider.bounds = Rect(left, top, right, bottom)
                    divider.draw(c)
                }
            }
        }
    }
}