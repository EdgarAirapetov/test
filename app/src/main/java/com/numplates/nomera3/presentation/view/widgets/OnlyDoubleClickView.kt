package com.numplates.nomera3.presentation.view.widgets

import android.content.Context
import android.graphics.Point
import android.graphics.PointF
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import androidx.core.view.GestureDetectorCompat
import com.meera.core.utils.listeners.DoubleOrOneClickListener
import kotlin.math.abs

const val DOUBLE_CLICK_TIME_DELAY: Long = DoubleOrOneClickListener.DOUBLE_CLICK_INTERVAL
const val DOUBLE_CLICK_ALLOWED_RADIUS: Int = 50

/**
 * Отрабатывает только двойной клик, остальное игнорирует и передает дальше
 * Используется в ситуациях, когда нужно добавить двойной клик,
 * не затрагивая остальные уже реализованные clickListener внутри родительского конейнера
 */
class OnlyDoubleClickView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : View(context, attrs, defStyle) {

    private var doubleClickListener: ((Point) -> Unit)? = null

    var lastClickTime: Long = 0
    var lastClickPoint: PointF? = null

    private val gestureListener = object : GestureDetector.SimpleOnGestureListener() {
        override fun onDown(e: MotionEvent): Boolean {
            if (e == null) return super.onDown(e)

            val clickTime = System.currentTimeMillis()
            val clickPoint = PointF(e.x, e.y)

            lastClickPoint?.let {
                if (clickTime - lastClickTime < DOUBLE_CLICK_TIME_DELAY &&
                    isTouchInSameArea(lastPoint = it, currentPoint = clickPoint)
                ) {
                    doubleClickListener?.invoke(Point(e.x.toInt(), e.y.toInt()))
                }
            }

            lastClickTime = clickTime
            lastClickPoint = clickPoint

            return super.onDown(e)
        }
    }

    private val gestureDetector: GestureDetectorCompat by lazy(LazyThreadSafetyMode.NONE) {
        GestureDetectorCompat(
            context,
            gestureListener
        )
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        gestureDetector.onTouchEvent(event)
        return super.onTouchEvent(event)
    }

    fun setOnDoubleClickListener(listener: (Point) -> Unit) {
        doubleClickListener = listener
    }

    fun removeOnDoubleClickListener() {
        doubleClickListener = null
    }

    private fun isTouchInSameArea(lastPoint: PointF, currentPoint: PointF): Boolean {
        val dx: Int = abs((currentPoint.x - lastPoint.x).toInt())
        val dy: Int = abs((currentPoint.y - lastPoint.y).toInt())
        return (dx * dx + dy * dy <= DOUBLE_CLICK_ALLOWED_RADIUS * DOUBLE_CLICK_ALLOWED_RADIUS)
    }
}
