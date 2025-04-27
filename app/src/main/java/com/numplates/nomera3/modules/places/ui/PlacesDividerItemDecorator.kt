package com.numplates.nomera3.modules.places.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import androidx.core.view.children
import androidx.recyclerview.widget.RecyclerView
import com.meera.core.extensions.dpToPx
import com.meera.core.extensions.getDrawableCompat
import com.numplates.nomera3.R

class PlacesDividerItemDecorator(context: Context) : RecyclerView.ItemDecoration() {

    private val divider = context.getDrawableCompat(R.drawable.drawable_divider_decoration_places)

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        divider?.let {
            val horizontalMargin = dpToPx(DIVIDER_MARGIN_START_DP)
            val left = parent.paddingLeft + horizontalMargin
            val right = parent.width - parent.paddingRight
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

    companion object {
        private const val DIVIDER_MARGIN_START_DP = 16
    }
}
