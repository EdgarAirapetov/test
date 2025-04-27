package com.numplates.nomera3.modules.comments.ui.util

import android.content.Context
import android.graphics.Rect
import android.util.DisplayMetrics
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView

private const val MILLISECONDS_PER_INCH = 10f //default is 25f (bigger = slower)

class SpeedyLinearLayoutManager(context: Context?) : LinearLayoutManager(context) {

    var isCanScrollVertically = true

    override fun smoothScrollToPosition(
        recyclerView: RecyclerView,
        state: RecyclerView.State, position: Int
    ) {

        val linearSmoothScroller: LinearSmoothScroller =
            object : LinearSmoothScroller(recyclerView.context) {

                override fun calculateSpeedPerPixel(displayMetrics: DisplayMetrics): Float =
                    MILLISECONDS_PER_INCH / displayMetrics.densityDpi

            }

        linearSmoothScroller.targetPosition = position
        startSmoothScroll(linearSmoothScroller)
    }

    override fun canScrollVertically(): Boolean {
        return isCanScrollVertically
    }

    override fun requestChildRectangleOnScreen(
        parent: RecyclerView,
        child: View, rect: Rect,
        immediate: Boolean,
        focusedChildVisible: Boolean
    ) = false
}
