package com.meera.core.views

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.core.widget.NestedScrollView

class NestedScrollViewDisabled : NestedScrollView {

    private var isTouchDisabled = false

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        if (isTouchDisabled) {
            return false
        } else {
            return super.onTouchEvent(ev)
        }
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        if (isTouchDisabled) {
            return false
        } else {
            return super.onInterceptTouchEvent(ev)
        }
    }

    fun setTouchDisabled(isTouchDisabled: Boolean) {
        this.isTouchDisabled = isTouchDisabled
    }

}
