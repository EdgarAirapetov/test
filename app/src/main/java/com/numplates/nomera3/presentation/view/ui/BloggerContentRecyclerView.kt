package com.numplates.nomera3.presentation.view.ui

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class BloggerContentRecyclerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RecyclerView(context, attrs, defStyleAttr) {

    var initialX = 0f
    var initialY = 0f

    override fun onInterceptTouchEvent(e: MotionEvent?): Boolean {
        if (e?.action == MotionEvent.ACTION_DOWN) {
            parent.requestDisallowInterceptTouchEvent(true)
            initialX = e.rawX
            initialY = e.rawY
        } else if (e?.action == MotionEvent.ACTION_MOVE) {
            val x = e.rawX - initialX
            val y = e.rawY - initialY
            val xDiff = kotlin.math.abs(x)
            val yDiff = kotlin.math.abs(y)
            checkDiffAndDisableTouchEvent(
                xDiff = xDiff,
                yDiff = yDiff
            )
        }
        return super.onInterceptTouchEvent(e)
    }

    fun getFirstVisiblePosition(): Int {
        return (layoutManager as LinearLayoutManager).findFirstCompletelyVisibleItemPosition()
    }

    private fun checkDiffAndDisableTouchEvent(xDiff: Float, yDiff: Float) {
        if (yDiff > xDiff) {
            parent.requestDisallowInterceptTouchEvent(false)
        }
    }
}
