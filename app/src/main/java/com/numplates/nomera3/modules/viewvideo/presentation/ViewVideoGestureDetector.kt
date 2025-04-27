package com.numplates.nomera3.modules.viewvideo.presentation

import android.content.Context
import android.graphics.Point
import android.graphics.PointF
import android.graphics.Rect
import android.os.Handler
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.VelocityTracker
import android.view.View
import android.view.ViewConfiguration
import androidx.core.graphics.minus
import com.meera.core.utils.graphics.SwipeDirectionDeterminator.Companion.isMotionBeyondTouchSlop
import com.meera.core.utils.graphics.SwipeDirectionDeterminator.Companion.reset
import kotlin.math.abs

private const val NORMAL_SEEK_VELOCITY_PX_PER_S = 225
private const val MIN_SEEK_ACCELERATION = 0.7f
private const val MAX_SEEK_ACCELERATION = 2.0f
private const val SEEK_CONSUME_THRESHOLD = 0.001f
private const val MIN_SCALE = 0.01f

/**
 * Used by [VelocityTracker] to output velocity in px/s
 */
private const val PIXELS_PER_SECOND = 1_000
private const val DOUBLE_TAP_DELAY = 300L

class ViewVideoGestureDetector(
    context: Context,
    private val doubleTapHandler: Handler?,
    private val timeBar: View?,
    private val listener: Listener) {

    private enum class GestureState {
        TAP,
        DOUBLE_TAP,
        LONG_TAP,
        LONG_TAP_DRAG,
        ZOOM,
        HORIZONTAL_SWIPE,
        HORIZONTAL_SWIPE_SEEK,
        VERTICAL_SWIPE;

        fun isBeyondTouchBoundsGesture() = this == LONG_TAP_DRAG || this == HORIZONTAL_SWIPE
    }

    private var enableEvents: Boolean = true

    private val touchSlopSquared: Int
    private val minFlingVelocity: Int
    private val maxFlingVelocity: Int
    private val longTapDelay: Long

    init {
        val viewConfiguration = ViewConfiguration.get(context)
        touchSlopSquared = viewConfiguration.scaledPagingTouchSlop.also { slop -> slop * slop }
        minFlingVelocity = viewConfiguration.scaledMinimumFlingVelocity
        maxFlingVelocity = viewConfiguration.scaledMaximumFlingVelocity
        longTapDelay = ViewConfiguration.getLongPressTimeout().toLong()
    }

    private var currentGesture: GestureState? = null

    private val downEventPoint: PointF = PointF()
    private val lastEventPoint: PointF = PointF()
    private var unconsumedSeek: Float = 0f

    private var scaleDetector: ScaleGestureDetector = ScaleGestureDetector(context, ScaleDetectorListener())
    private var velocityTracker: VelocityTracker? = null

    private var lastEvent: MotionEvent? = null

    private val checkForLongTap = Runnable {
        if (currentGesture == GestureState.TAP) {
            currentGesture = GestureState.LONG_TAP
            if (checkIfLastEventInsideTimeBar()) {
                lastEvent?.let { event ->
                    val tapPoint = Point(event.x.toInt(), event.y.toInt())
                    listener.onLongTapTimeBar(tapPoint)
                }
            } else {
                listener.onLongTap()
            }
        }
    }

    fun setTouchEventsEnabled(isEnabled: Boolean) {
        enableEvents = isEnabled
    }

    fun isTouchesEnabled() = enableEvents

    fun onTouchEvent(view: View, event: MotionEvent): Boolean {
        if (enableEvents.not()) {
            if (currentGesture == GestureState.LONG_TAP) listener.onLongTapReleased()
            currentGesture = null
            return false
        }
        checkLastActionZoom(event)
        scaleDetector.onTouchEvent(event)
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                val isDoubleTapExpecting = isDoubleTapExpecting()
                stopDoubleTapHandler()
                currentGesture = if (isDoubleTapExpecting) GestureState.DOUBLE_TAP else GestureState.TAP
                downEventPoint.set(event.x, event.y)
                lastEvent = event
                if (checkIfLastEventInsideTimeBar()) listener.onTapInsideTimeBar()
                view.postDelayed(checkForLongTap, longTapDelay)
                view.disallowIntercept()
                velocityTracker = velocityTracker ?: VelocityTracker.obtain()
                velocityTracker?.addMovement(event)
                if (!isDoubleTapExpecting) {
                    listener.onTap()
                }
            }
            MotionEvent.ACTION_MOVE -> {
                checkTouchBoundsForTap(view, event)
                velocityTracker?.addMovement(event)

                when (currentGesture) {
                    GestureState.LONG_TAP_DRAG, GestureState.HORIZONTAL_SWIPE_SEEK -> onLongTapDragging(currentEvent = event, viewWidth = view.width)
                    else -> Unit
                }

                lastEventPoint.set(event.x, event.y)
            }
            MotionEvent.ACTION_UP -> {
                view.allowIntercept()
                when (currentGesture) {
                    GestureState.TAP -> tapActions(view)
                    GestureState.DOUBLE_TAP -> listener.onDoubleTap()
                    GestureState.LONG_TAP,
                    GestureState.LONG_TAP_DRAG -> listener.onLongTapReleased()
                    GestureState.HORIZONTAL_SWIPE_SEEK -> checkForHorizontalFling()
                    GestureState.VERTICAL_SWIPE -> checkForVerticalFling()
                    else -> Unit
                }
                reset(view)
            }
            MotionEvent.ACTION_CANCEL -> {
                view.allowIntercept()
                when (currentGesture) {
                    GestureState.TAP -> listener.onTapReleased()
                    GestureState.LONG_TAP,
                    GestureState.LONG_TAP_DRAG -> listener.onLongTapReleased()
                    else -> Unit
                }
                reset(view)
            }
        }
        return true
    }

    private fun checkLastActionZoom(event: MotionEvent) {
        if (currentGesture == GestureState.ZOOM && event.pointerCount < 2) {
            listener.onScaleEnd()
        }
    }

    private fun tapActions(view: View) {
        if (checkIfLastEventInsideTimeBar()) return
        startDoubleTapHandler(action = {
            view.performClick()
            listener.onTapReleased()
        })
    }

    private fun startDoubleTapHandler(action: () -> Unit) {
        stopDoubleTapHandler()
        doubleTapHandler?.postDelayed({ action.invoke() }, DOUBLE_TAP_DELAY)
    }

    private fun stopDoubleTapHandler() {
        doubleTapHandler?.removeCallbacksAndMessages(null)
        doubleTapHandler?.hasMessages(0)
    }

    private fun isDoubleTapExpecting() = doubleTapHandler?.hasMessages(0) ?: false

    private fun checkIfLastEventInsideTimeBar(): Boolean {
        val outRect = Rect()
        val location = IntArray(2)

        val lastEvent = lastEvent ?: return false
        val timeBar = timeBar ?: return false

        timeBar.getDrawingRect(outRect)
        timeBar.getLocationOnScreen(location)
        outRect.offset(location[0], location[1])

        return outRect.contains(lastEvent.x.toInt(), lastEvent.y.toInt())
    }

    private fun checkTouchBoundsForTap(view: View, event: MotionEvent) {
        if (currentGesture?.isBeyondTouchBoundsGesture() == true
            || event.pointerCount > 1
            || currentGesture == GestureState.HORIZONTAL_SWIPE_SEEK) return
        val eventPoint = PointF(event.x, event.y)
        val distance = eventPoint - downEventPoint
        if (isMotionBeyondTouchSlop(touchSlopSquared = touchSlopSquared, distanceTraveled = distance)) {
            view.removeCallbacks(checkForLongTap)
            when {
                currentGesture == GestureState.LONG_TAP -> currentGesture = GestureState.LONG_TAP_DRAG
                currentGesture != GestureState.ZOOM && isHorizontalMovement(distance) -> setupHorizontalCurrentGesture()
                currentGesture != GestureState.ZOOM && isVerticalMovement(distance) -> currentGesture = GestureState.VERTICAL_SWIPE
            }
            allowInterceptIfInitialGesture(view)
        }
    }

    private fun onLongTapDragging(currentEvent: MotionEvent, viewWidth: Int) {
        val deltaX = currentEvent.x - lastEventPoint.x
        val velocity = run {
            velocityTracker?.computeCurrentVelocity(PIXELS_PER_SECOND)
            velocityTracker?.xVelocity ?: 0f
        }

        val coefficient = abs(velocity / NORMAL_SEEK_VELOCITY_PX_PER_S)
        val correctedCoefficient = coefficient.coerceIn(MIN_SEEK_ACCELERATION, MAX_SEEK_ACCELERATION)

        val relativeMovement = deltaX / viewWidth
        val relativeMovementAccelerated = relativeMovement * correctedCoefficient

        unconsumedSeek += relativeMovementAccelerated

        if (abs(unconsumedSeek) >= SEEK_CONSUME_THRESHOLD) {
            listener.seekTo(currentEvent.x.toInt())
            unconsumedSeek = 0f
        }
    }

    private fun setupHorizontalCurrentGesture() {
        currentGesture = if (checkIfLastEventInsideTimeBar()) {
            GestureState.HORIZONTAL_SWIPE_SEEK
        } else {
            GestureState.HORIZONTAL_SWIPE
        }

        if (currentGesture == GestureState.HORIZONTAL_SWIPE_SEEK) {
            lastEvent?.let { event ->
                val tapPoint = Point(event.x.toInt(), event.y.toInt())
                listener.onHorizontalSwipeTimeBar(tapPoint)
            }
        }
    }

    private fun onHorizontalSwipe(currentX: Float) {
        val scrollX = currentX - lastEventPoint.x
        if (abs(scrollX) < 1) return
        listener.onHorizontalSwipe(scrollX)
    }

    private fun checkForHorizontalFling() {
        velocityTracker?.computeCurrentVelocity(PIXELS_PER_SECOND, maxFlingVelocity.toFloat())
        val xVelocity = velocityTracker?.xVelocity ?: 0f
        if (abs(xVelocity) > minFlingVelocity) {
            listener.onHorizontalFling(xVelocity)
        } else {
            listener.onHorizontalSwipeEnded()
        }
    }

    private fun checkForVerticalFling() {
        velocityTracker?.computeCurrentVelocity(PIXELS_PER_SECOND, maxFlingVelocity.toFloat())
        val yVelocity = velocityTracker?.yVelocity ?: 0f
        if (abs(yVelocity) > minFlingVelocity) {
            listener.onVerticalFling(yVelocity)
        }
    }

    private fun allowInterceptIfInitialGesture(view: View) {
        if (currentGesture == null || currentGesture == GestureState.TAP) {
            view.allowIntercept()
        }
    }

    private fun reset(view: View) {
        view.removeCallbacks(checkForLongTap)
        currentGesture = null
        downEventPoint.reset()
        lastEventPoint.reset()
        velocityTracker?.recycle()
        velocityTracker = null
        unconsumedSeek = 0f
    }

    private fun isHorizontalMovement(distance: PointF) = abs(distance.x) > abs(distance.y)
    private fun isVerticalMovement(distance: PointF) = abs(distance.x) < abs(distance.y)

    private fun View.disallowIntercept() {
        parent.requestDisallowInterceptTouchEvent(true)
    }

    private fun View.allowIntercept() {
        parent.requestDisallowInterceptTouchEvent(false)
    }

    private inner class ScaleDetectorListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {

        override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
            return if (currentGesture == null || currentGesture == GestureState.TAP || currentGesture == GestureState.ZOOM) {
                listener.onScaleBegin(detector.focusX, detector.focusY)
                currentGesture = GestureState.ZOOM
                true
            } else {
                false
            }
        }

        override fun onScale(detector: ScaleGestureDetector): Boolean {
            val scaleFactor = detector.scaleFactor
            if (scaleFactor < MIN_SCALE) {
                return false
            } else {
                listener.onScale(scaleFactor)
            }
            return super.onScale(detector)
        }

        override fun onScaleEnd(detector: ScaleGestureDetector) {
            val pointerCount = lastEvent?.pointerCount ?: 0
            if (pointerCount > 1) return
            listener.onScaleEnd()
            currentGesture = null
        }
    }

    interface Listener {
        fun onTap()
        fun onTapReleased()
        fun onDoubleTap()
        fun onLongTap()
        fun onLongTapTimeBar(tapPoint: Point)
        fun seekTo(positionX: Int)
        fun onLongTapReleased()
        fun onScaleBegin(focusX: Float, focusY: Float)
        fun onScale(scale: Float)
        fun onScaleEnd()
        fun onHorizontalSwipe(distanceX: Float)
        fun onHorizontalSwipeTimeBar(tapPoint: Point)
        fun onHorizontalSwipeEnded()
        fun onHorizontalFling(velocityX: Float)
        fun onVerticalFling(velocityY: Float)
        fun onTapInsideTimeBar()
    }
}
