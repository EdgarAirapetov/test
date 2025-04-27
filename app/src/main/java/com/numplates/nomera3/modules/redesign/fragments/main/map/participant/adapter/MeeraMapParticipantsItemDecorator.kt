package com.numplates.nomera3.modules.redesign.fragments.main.map.participant.adapter

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import androidx.core.view.children
import androidx.recyclerview.widget.RecyclerView
import com.meera.core.extensions.dpToPx
import com.meera.core.extensions.getDrawableCompat
import com.numplates.nomera3.R

class MeeraMapParticipantsItemDecorator(context: Context) : RecyclerView.ItemDecoration() {

    private val divider = context.getDrawableCompat(R.drawable.meera_drawable_divider_decoration_participants)

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        divider?.let {
            val horizontalMargin = dpToPx(DIVIDER_MARGIN_START_DP)
            val left = parent.paddingLeft + horizontalMargin
            val right = parent.width - dpToPx(24)
            parent.children.forEach { view ->
                val childPosition = parent.getChildAdapterPosition(view)
                val itemCount = parent.adapter?.itemCount ?: 0
                if (childPosition < itemCount - 1) {
                    val params = view.layoutParams as RecyclerView.LayoutParams

                    val top = view.bottom + params.bottomMargin + DIVIDER_MARGIN_BOTTOM_DP
                    val bottom = top + divider.intrinsicHeight

                    divider.bounds = Rect(left, top, right, bottom)
                    divider.draw(c)
                }
            }
        }
    }

    companion object {
        private const val DIVIDER_MARGIN_START_DP = 76
        private const val DIVIDER_MARGIN_BOTTOM_DP = 20
    }
}
