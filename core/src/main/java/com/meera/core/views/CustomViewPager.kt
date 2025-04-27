package com.meera.core.views

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.viewpager.widget.ViewPager

enum class SwipeDirection {
    ALL, LEFT, RIGHT, NONE
}

class CustomViewPager(context: Context, attrs: AttributeSet) : ViewPager(context, attrs) {

    private var swipeEnabled = false
    private var swipeDirection = SwipeDirection.ALL
    private var initialXValue: Float = 0f

    init {
        this.swipeEnabled = true
    }


    override fun onTouchEvent(event: MotionEvent): Boolean {
        return if (this.swipeEnabled && this.isSwipeAllowed(event)) {
            super.onTouchEvent(event)
        } else false

    }


    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        return if (this.swipeEnabled && this.isSwipeAllowed(event)) {
            super.onInterceptTouchEvent(event)
        } else false
    }


    private fun isSwipeAllowed(event: MotionEvent): Boolean {
        if (this.swipeDirection == SwipeDirection.ALL) return true

        if (this.swipeDirection == SwipeDirection.NONE) return false  //disable any swipe

        if (event.action == MotionEvent.ACTION_DOWN) {
            initialXValue = event.x;
            return true
        }

        if (event.action == MotionEvent.ACTION_MOVE) {
            val diffX = event.x - initialXValue
            if (diffX > 0 && swipeDirection === SwipeDirection.RIGHT) {
                // swipe from left to right detected
                return false
            } else if (diffX < 0 && swipeDirection === SwipeDirection.LEFT) {
                // swipe from right to left detected
                return false
            }
        }

        return true
    }


    fun setPagingEnabled(enabled: Boolean) {
        this.swipeEnabled = enabled
    }

    fun setAllowedSwipeDirection(direction: SwipeDirection) {
        this.swipeDirection = direction
    }
}
