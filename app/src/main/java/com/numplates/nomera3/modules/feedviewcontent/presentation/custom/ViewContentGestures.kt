package com.numplates.nomera3.modules.feedviewcontent.presentation.custom

import android.graphics.PointF
import android.view.MotionEvent
import android.view.View
import androidx.viewpager2.widget.ViewPager2
import com.meera.core.utils.graphics.SwipeDirectionDeterminator.Companion.reset
import com.meera.core.utils.graphics.SwipeDirectionDeterminator.Companion.sub
import com.numplates.nomera3.presentation.view.ui.ExtendedGestureOverlayView
import com.numplates.nomera3.presentation.view.ui.GestureEventPasser
import kotlin.math.abs

private const val MIN_HEIGHT_TO_VERTICAL_SWIPE = 60

class ViewContentGestures {

    var onVerticalSwipe: () -> Unit = {}
    var isTouchesBlocked = false
    var isMultiTouchFound = false

    private var extendedGestureOverlayView: ExtendedGestureOverlayView? = null
    private var viewPager2: ViewPager2? = null
    private var lastValue = PointF(0f, 0f)
    private var lastDelta = PointF(0f, 0f)

    fun initGesturesInterceptor(
        extendedGestureOverlayView: ExtendedGestureOverlayView?,
        viewPager2: ViewPager2?
    ) {
        initViews(extendedGestureOverlayView = extendedGestureOverlayView, viewPager2 = viewPager2)
        initGestures()
    }

    fun destroyGesturesInterceptor() {
        viewPager2?.isUserInputEnabled = true
        extendedGestureOverlayView?.setGestureEventPasser(null)
        extendedGestureOverlayView = null
        viewPager2 = null
    }

    private fun initViews(
        extendedGestureOverlayView: ExtendedGestureOverlayView?,
        viewPager2: ViewPager2?
    ) {
        this.extendedGestureOverlayView = extendedGestureOverlayView
        this.viewPager2 = viewPager2
    }

    private fun initGestures() {
        viewPager2?.isUserInputEnabled = false
        extendedGestureOverlayView?.addExtendedGestureListener()
        extendedGestureOverlayView?.setGestureEventPasser(object : GestureEventPasser {
            override fun onGesturePassed(view: View?, event: MotionEvent?) {
                if (event == null || view == null) return
                handleOnTouchEvent(event)
            }
        })
    }

    private fun handleOnTouchEvent(event: MotionEvent): Boolean {
        if (isTouchesBlocked) return true
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                isMultiTouchFound = false
                lastValue.set(event.rawX, event.rawY)
                if (viewPager2?.isFakeDragging == false) viewPager2?.beginFakeDrag()
            }
            MotionEvent.ACTION_MOVE -> {
                if (event.pointerCount > 1) isMultiTouchFound = true
                if (isMultiTouchFound) return true
                val newValue = PointF(event.rawX, event.rawY)
                val delta = newValue sub lastValue
                if (viewPager2?.isFakeDragging == true || viewPager2?.beginFakeDrag() == true) {
                    viewPager2?.fakeDragBy(delta.x)
                }
                lastValue.set(newValue)
                lastDelta.set(delta)
            }
            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                viewPager2?.endFakeDrag()
                if (getLastDeltaVerticalLength() > MIN_HEIGHT_TO_VERTICAL_SWIPE) onVerticalSwipe.invoke()
                lastDelta.reset()
            }
        }
        return true
    }

    private fun getLastDeltaVerticalLength() = abs(lastDelta.y)
}

