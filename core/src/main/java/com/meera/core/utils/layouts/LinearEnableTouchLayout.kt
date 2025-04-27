package com.meera.core.utils.layouts

import android.content.Context
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.MotionEvent
import android.widget.LinearLayout

/**
 * Блокирует нажатия себя и своих детей
 */
class LinearEnableTouchLayout @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAtr: Int = 0
) : LinearLayout(context, attributeSet, defStyleAtr) {

    var enableTouchEvents = true

    override fun dispatchTouchEvent(e: MotionEvent?): Boolean {
        return if (enableTouchEvents.not()) {
            true
        } else {
            super.dispatchTouchEvent(e)
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return if (enableTouchEvents.not()) {
            true
        } else {
            super.onKeyDown(keyCode, event)
        }
    }
}

