package com.numplates.nomera3.presentation.view.fragments

import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration


class HorizontalLineDivider(
    private val dividerDrawable: Drawable,
    private val paddingLeft: Int = 0,
    private val paddingRight: Int = 0
) : ItemDecoration() {

    constructor(dividerDrawable: Drawable, horizontalPaddingDp: Int): this(
        dividerDrawable = dividerDrawable,
        paddingLeft = horizontalPaddingDp,
        paddingRight = horizontalPaddingDp
    )

    override fun onDrawOver(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        // по умолчанию
        // dividerLeft = 0
        // dividerRight = ширина экрана
        val childCount = parent.childCount
        val dividerLeft = parent.paddingLeft + paddingLeft
        val dividerRight = parent.width - parent.paddingLeft - paddingRight

        for (i in 0..childCount - 2) {
            val child: View = parent.getChildAt(i)
            val params = child.layoutParams as RecyclerView.LayoutParams
            val dividerTop: Int = child.bottom + params.bottomMargin
            val dividerBottom: Int = dividerTop + dividerDrawable.intrinsicHeight

            dividerDrawable.setBounds(dividerLeft, dividerTop, dividerRight, dividerBottom)

            dividerDrawable.draw(canvas)
        }
    }
}
