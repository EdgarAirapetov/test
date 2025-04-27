package com.numplates.nomera3.modules.moments.show.presentation.custom

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PointF
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import android.view.ViewConfiguration
import com.meera.core.utils.graphics.SwipeDirectionDeterminator.Companion.getRadiusVector
import kotlin.math.abs

private const val MOMENT_CHANGE_CLICK_TIMEOUT = 150L
private const val MOMENT_LONG_TAP_MS = 1500L
private const val SWIPE_THRESHOLD = 100
private const val SWIPE_THRESHOLD_X = 10
private const val SWIPE_VELOCITY_THRESHOLD = 100
private const val UNITS_PIXELS_PER_SECONDS = 1000

@Suppress("DEPRECATION")
class MomentTouchHandler(
    context: Context,
    private val onContentTap: () -> Unit,
    private val onContentTapRelease: () -> Unit,
    private val onClickLeft: () -> Unit,
    private val onClickRight: () -> Unit,
    private val onContentLongTap: () -> Unit,
    private val onSwipeDown: () -> Unit
) : View.OnTouchListener {

    private val handler = Handler(Looper.getMainLooper())
    private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop // TODO use square of the value?

    private val minimumFlingVelocity = ViewConfiguration.get(context).scaledMinimumFlingVelocity
    private val maximumFlingVelocity = ViewConfiguration.get(context).scaledMaximumFlingVelocity

    private val initialPoint = PointF(0f, 0f)

    private var currentDownEvent: MotionEvent? = null
    private var velocityTracker: VelocityTracker? = null
    private var isMoving = false


    private val longTapRunnable = Runnable {
        onContentLongTap.invoke()
    }

    @SuppressLint("Recycle", "ClickableViewAccessibility")
    override fun onTouch(v: View, event: MotionEvent): Boolean {
        velocityTracker = velocityTracker ?: VelocityTracker.obtain()
        velocityTracker?.addMovement(event)
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                initialPoint.set(event.x, event.y)
                currentDownEvent?.recycle()
                currentDownEvent = MotionEvent.obtain(event)
                isMoving = false

                onContentTap.invoke()
                handler.postDelayed(longTapRunnable, MOMENT_LONG_TAP_MS)
            }

            MotionEvent.ACTION_MOVE -> {
                if (!isMoving) {
                    isMoving = getRadiusVector(initialPoint, PointF(event.x, event.y)) > touchSlop
                    if (isMoving) {
                        handler.removeCallbacks(longTapRunnable)
                    }
                }
            }

            MotionEvent.ACTION_CANCEL,
            MotionEvent.ACTION_UP -> {
                onContentTapRelease.invoke()
                handler.removeCallbacks(longTapRunnable)
                checkClickEvent(v, event)
                checkFlingEvent(event)
                velocityTracker?.recycle()
                velocityTracker = null
            }
        }
        return true
    }

    private fun checkClickEvent(view: View, event: MotionEvent) {
        if (event.eventTime - event.downTime < MOMENT_CHANGE_CLICK_TIMEOUT && !isMoving) {
            val isLeftSideArea = event.x < (view.width / 2)
            if (isLeftSideArea) {
                onClickLeft.invoke()
            } else {
                onClickRight.invoke()
            }
        }
    }

    private fun checkFlingEvent(event: MotionEvent) {
        val velocityTracker = velocityTracker ?: return
        val pointerId = event.getPointerId(0)
        velocityTracker.computeCurrentVelocity(UNITS_PIXELS_PER_SECONDS, maximumFlingVelocity.toFloat())
        val velocityY = velocityTracker.getYVelocity(pointerId)
        val velocityX = velocityTracker.getXVelocity(pointerId)
        if (abs(velocityY) > minimumFlingVelocity || abs(velocityX) > minimumFlingVelocity) {
            handleFlingEvent(currentDownEvent!!, event, velocityY)
        }
    }

    private fun handleFlingEvent(e1: MotionEvent, e2: MotionEvent, velocityY: Float) {
        val diffY = e2.y - e1.y
        val diffX = abs(e2.x - e1.x)
        if (
            abs(diffY) > SWIPE_THRESHOLD
            && diffX < SWIPE_THRESHOLD_X
            && abs(velocityY) > SWIPE_VELOCITY_THRESHOLD
            && diffY > 0
            && abs(diffY) > diffX
        ) {
            onSwipeDown.invoke()
        }
    }
}

