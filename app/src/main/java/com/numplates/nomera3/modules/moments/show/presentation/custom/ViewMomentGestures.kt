package com.numplates.nomera3.modules.moments.show.presentation.custom

import android.graphics.PointF
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import androidx.viewpager2.widget.ViewPager2
import com.meera.core.utils.graphics.SwipeDirectionDeterminator
import com.meera.core.utils.graphics.SwipeDirectionDeterminator.Companion.getDirection
import com.meera.core.utils.graphics.SwipeDirectionDeterminator.Companion.getRadiusVector
import com.meera.core.utils.graphics.SwipeDirectionDeterminator.Companion.reset
import com.meera.core.utils.graphics.SwipeDirectionDeterminator.Companion.sub
import com.numplates.nomera3.presentation.view.ui.ExtendedGestureOverlayView
import com.numplates.nomera3.presentation.view.ui.GestureEventPasser

private const val MAX_WIDTH_TO_SWIPE_RIGHT = -25
private const val MIN_WIDTH_TO_SWIPE_LEFT = 25

class ViewMomentGestures {

    var onDragEnd: (SwipeDirection?) -> Unit = {}
    var onDragStart: (isHorizontal: Boolean) -> Unit = {}

    private var touchSlop = 0

    private var extendedGestureOverlayView: ExtendedGestureOverlayView? = null
    private var viewPager2: ViewPager2? = null
    private var isSwipe = false
    private var lastValue = PointF(0f, 0f)
    private var lastDelta = PointF(0f, 0f)
    private var downValue = PointF(0f, 0f)
    private var available = true

    fun initGesturesInterceptor(
        extendedGestureOverlayView: ExtendedGestureOverlayView?,
        viewPager2: ViewPager2?
    ) {
        this.extendedGestureOverlayView = extendedGestureOverlayView
        this.viewPager2 = viewPager2
        activateGestures()
    }

    fun destroyGesturesInterceptor() {
        viewPager2?.isUserInputEnabled = true
        extendedGestureOverlayView?.setGestureEventPasser(null)
        extendedGestureOverlayView = null
        viewPager2 = null
    }

    fun toggleGesturesAvailability(available: Boolean) {
        this.available = available
    }

    private fun activateGestures() {
        viewPager2?.isUserInputEnabled = false
        extendedGestureOverlayView?.addExtendedGestureListener()
        extendedGestureOverlayView?.setGestureEventPasser(object : GestureEventPasser {
            override fun onGesturePassed(view: View?, event: MotionEvent?) {
                if (event == null || view == null) return
                handleOnTouchEvent(view = view, event = event)
            }
        })
    }

    private fun handleOnTouchEvent(view: View, event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                if (!available) return true
                isSwipe = false
                touchSlop = ViewConfiguration.get(view.context).scaledTouchSlop
                lastValue.set(event.rawX, event.rawX)
                downValue.set(event.rawX, event.rawY)
                if (viewPager2?.isFakeDragging == false) viewPager2?.beginFakeDrag()
            }
            MotionEvent.ACTION_MOVE -> {
                if (!available) return true
                val newValue = PointF(event.rawX, event.rawY)
                val delta = newValue sub lastValue
                checkIfHorizontalSwipe(newValue)
                if (viewPager2?.isFakeDragging == true || viewPager2?.beginFakeDrag() == true) {
                    viewPager2?.fakeDragBy(delta.x)
                }
                lastValue = newValue
                lastDelta = delta
            }
            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                val currentValue = PointF(event.rawX, event.rawY)
                val diffValue = currentValue sub downValue
                viewPager2?.endFakeDrag()
                when {
                    diffValue.x > MIN_WIDTH_TO_SWIPE_LEFT -> onDragEnd.invoke(SwipeDirection.LEFT)
                    diffValue.x < MAX_WIDTH_TO_SWIPE_RIGHT -> onDragEnd.invoke(SwipeDirection.RIGHT)
                    else -> onDragEnd.invoke(null)
                }
                lastDelta.reset()
            }
        }
        return true
    }

    private fun checkIfHorizontalSwipe(newValue: PointF) {
        if (isSwipe) return
        val radiusVector = getRadiusVector(pointA = lastValue, pointB = newValue)
        isSwipe = touchSlop < radiusVector
        val direction = getDirection(pointA = lastValue, pointB = newValue)
        onDragStart.invoke(
            direction == SwipeDirectionDeterminator.RIGHT || direction == SwipeDirectionDeterminator.LEFT
        )
    }
}

enum class SwipeDirection {
    LEFT, RIGHT
}
