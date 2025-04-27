package com.meera.core.utils.layouts.intercept

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.constraintlayout.widget.ConstraintLayout


class InterceptTouchConstraintLayout @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAtr: Int = 0
) : ConstraintLayout(context, attributeSet, defStyleAtr), InterceptTouchLayout {

    private var dispatchEventPasser: ((MotionEvent) -> Boolean)? = null

    override fun dispatchTouchEvent(e: MotionEvent?): Boolean {
        val event = e ?: return false

        return dispatchEventPasser?.let {
            dispatchEventPasser?.invoke(event)
            true
        } ?: kotlin.run {
            super.dispatchTouchEvent(event)
        }
    }

    override fun bypassTouches(dispatchEventPasser: ((MotionEvent) -> Boolean)?) {
        this.dispatchEventPasser = dispatchEventPasser
    }
}

