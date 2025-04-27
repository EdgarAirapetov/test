package com.meera.core.utils.layouts

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.FrameLayout

class FrameTouchEventInterceptorLayout @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAtr: Int = 0
) : FrameLayout(context, attributeSet, defStyleAtr) {

    var isEnabledTouchInterceptor = false

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        val handled = super.dispatchTouchEvent(ev)
        if (isEnabledTouchInterceptor) requestDisallowInterceptTouchEvent(true)
        return handled
    }

    fun enableTouchEventIntercept() {
        isEnabledTouchInterceptor = true
    }
}

