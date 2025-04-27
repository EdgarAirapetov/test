package com.meera.core.utils

import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.view.MotionEvent
import android.view.View
import android.widget.TextView

private const val LEFT = 0
private const val TOP = 1
private const val RIGHT = 2
private const val BOTTOM = 3

/**
 * Handles compound drawable touch events.
 * Will intercept every event that happened inside (calculated) compound drawable bounds, extended by fuzz.
 * @see TextView#getCompoundDrawables()
 * @see TextView#setCompoundDrawablesRelativeWithIntrinsicBounds(int, int, int, int)
 */
abstract class CompoundDrawableTouchListener constructor(
    private val fuzz: Int = 0
) : View.OnTouchListener {

    private val drawableIndexes = intArrayOf(LEFT, TOP, RIGHT, BOTTOM)

    override fun onTouch(view: View?, event: MotionEvent): Boolean {
        if (view !is TextView) {
            return false
        }
        val drawables = view.compoundDrawables
        val x = event.x.toInt()
        val y = event.y.toInt()
        for (i in drawableIndexes) {
            if (drawables[i] == null) continue
            val bounds = getRelativeBounds(i, drawables[i], view)
            val fuzzedBounds = addFuzz(bounds)
            if (fuzzedBounds.contains(x, y)) {
                val relativeEvent = MotionEvent.obtain(
                    event.downTime,
                    event.eventTime,
                    event.action,
                    event.x - bounds.left,
                    event.y - bounds.top,
                    event.metaState
                )
                return onDrawableTouch(view, i, bounds, relativeEvent)
            }
        }
        return false
    }

    /**
     * Calculates compound drawable bounds relative to wrapping view
     * @param index compound drawable index
     * @param drawable the drawable
     * @param view wrapping view
     * @return [Rect] with relative bounds
     */
    private fun getRelativeBounds(index: Int, drawable: Drawable, view: View): Rect {
        val drawableBounds = drawable.bounds
        val bounds = Rect(drawableBounds)
        when (index) {
            LEFT -> bounds.offsetTo(
                view.paddingLeft,
                view.height / 2 - bounds.height() / 2
            )
            TOP -> bounds.offsetTo(
                view.width / 2 - bounds.width() / 2,
                view.paddingTop
            )
            RIGHT -> bounds.offsetTo(
                view.width - view.paddingRight - bounds.width(),
                view.height / 2 - bounds.height() / 2
            )
            BOTTOM -> bounds.offsetTo(
                view.width / 2 - bounds.width() / 2,
                view.height - view.paddingBottom - bounds.height()
            )
        }
        return bounds
    }

    /**
     * Expands [Rect] by given value in every direction relative to its center
     * @param source given [Rect]
     * @return result [Rect]
     */
    private fun addFuzz(source: Rect): Rect {
        val result = Rect()
        result.left = source.left - fuzz
        result.right = source.right + fuzz
        result.top = source.top - fuzz
        result.bottom = source.bottom + fuzz
        return result
    }

    /**
     * Compound drawable touch-event handler
     * @param v wrapping view
     * @param drawableIndex index of compound drawable which recicved the event
     * @param drawableBounds [Rect] with compound drawable bounds relative to wrapping view. Fuzz not included
     * @param event event with coordinated relative to wrapping view - i.e. within `drawableBounds`. If using fuzz, may return negative coordinates.
     */
    protected abstract fun onDrawableTouch(
        v: View?,
        drawableIndex: Int,
        drawableBounds: Rect?,
        event: MotionEvent?
    ): Boolean
}
