package com.meera.core.utils

import android.graphics.Rect
import android.view.MotionEvent
import android.view.View
import com.meera.core.extensions.DEFAULT_DRAWABLE_FUZZ
import com.meera.core.extensions.dp

abstract class CompoundDrawableClickListener constructor(
    fuzz: Int = DEFAULT_DRAWABLE_FUZZ.dp
) : CompoundDrawableTouchListener(fuzz) {

    override fun onDrawableTouch(
        v: View?,
        drawableIndex: Int,
        drawableBounds: Rect?,
        event: MotionEvent?
    ): Boolean {
        if (event?.action == MotionEvent.ACTION_UP) onDrawableClick(v, drawableIndex)
        return true
    }

    /**
     * Compound drawable touch-event handler
     * @param v wrapping view
     * @param drawableIndex index of compound drawable which recicved the event
     */
    protected abstract fun onDrawableClick(v: View?, drawableIndex: Int)
}
