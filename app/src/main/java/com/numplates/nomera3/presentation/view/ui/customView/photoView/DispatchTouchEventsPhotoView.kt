package com.numplates.nomera3.presentation.view.ui.customView.photoView

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.MotionEvent
import com.github.chrisbanes.photoview.PhotoView

const val LONG_PRESS_TIME_IN_MILLISECONDS = 1000L

class DispatchTouchEventsPhotoView(context: Context, attrs: AttributeSet?) : PhotoView(context, attrs) {

    private var gestureListener: OnPhotoViewGestureListener? = null

    fun setGestureListener(listener: OnPhotoViewGestureListener) {
        this.gestureListener = listener
    }

    private val longTapHandler = Handler(Looper.getMainLooper())
    private var isLongTap = false
    private val longPressRunnable = Runnable {
        isLongTap = true
        gestureListener?.onLongTap()
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                isLongTap = false
                longTapHandler.postDelayed(longPressRunnable, LONG_PRESS_TIME_IN_MILLISECONDS)
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                longTapHandler.removeCallbacks(longPressRunnable)
                if (isLongTap) {
                    gestureListener?.onLongTapRelease()
                }
            }
        }

        return super.dispatchTouchEvent(event)
    }
}

