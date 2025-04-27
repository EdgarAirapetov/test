package com.numplates.nomera3.modules.userprofile.ui

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ViewConfiguration
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

class CustomSwipeToRefresh @JvmOverloads constructor(
    context: Context, attributeSet: AttributeSet? = null
) : SwipeRefreshLayout(context, attributeSet) {

    private var mTouchSlop: Int
    private var mPrevX: Float = 0f

    init {
        mTouchSlop = ViewConfiguration.get(context).scaledTouchSlop
    }


    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {

        when (ev.action) {
            MotionEvent.ACTION_DOWN -> mPrevX = MotionEvent.obtain(ev).x

            MotionEvent.ACTION_MOVE -> {
                val eventX = ev.x
                val diff = Math.abs(eventX - mPrevX)
                if (diff > mTouchSlop) return false
            }
        }


        return super.onInterceptTouchEvent(ev)
    }
}
