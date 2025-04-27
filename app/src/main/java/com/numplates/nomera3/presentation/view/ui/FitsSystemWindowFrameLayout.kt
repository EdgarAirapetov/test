package com.numplates.nomera3.presentation.view.ui

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.WindowInsets
import android.widget.FrameLayout

import androidx.annotation.RequiresApi

class FitsSystemWindowFrameLayout : FrameLayout {

    lateinit var onTouch: (ev: MotionEvent?) -> Unit

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        if (::onTouch.isInitialized)
            onTouch.invoke(ev)
        return super.onInterceptTouchEvent(ev)
    }

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet,
                defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    constructor(context: Context, attrs: AttributeSet,
                defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)

    override fun onApplyWindowInsets(insets: WindowInsets): WindowInsets {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setPadding(insets.systemWindowInsetLeft, 0, insets.systemWindowInsetRight,
                    insets.systemWindowInsetBottom)
            return insets.replaceSystemWindowInsets(0, 0, 0, 0)
        } else {
            return super.onApplyWindowInsets(insets)
        }
    }


}
