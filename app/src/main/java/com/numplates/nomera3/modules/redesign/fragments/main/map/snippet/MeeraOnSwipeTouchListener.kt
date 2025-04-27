package com.numplates.nomera3.modules.redesign.fragments.main.map.snippet

import android.view.MotionEvent
import android.view.MotionEvent.ACTION_CANCEL
import android.view.MotionEvent.ACTION_DOWN
import android.view.MotionEvent.ACTION_MOVE
import android.view.View

private const val SWIPE_THRESHOLD = 8

abstract class MeeraOnSwipeTouchListener : View.OnTouchListener {

    private var swipeStart = 0f
    private var swipeEnd = 0f
    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        when (event?.action) {
            ACTION_DOWN -> {
                swipeStart = event.y
            }

            ACTION_MOVE -> {
                swipeEnd = event.y
            }

            ACTION_CANCEL -> {
                if ((swipeEnd - swipeStart) > SWIPE_THRESHOLD) {
                    onSwipeDown()
                }
            }
        }
        return true
    }

    abstract fun onSwipeDown()
}
