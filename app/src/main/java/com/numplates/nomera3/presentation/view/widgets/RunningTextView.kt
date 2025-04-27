package com.numplates.nomera3.presentation.view.widgets

import android.content.Context
import android.graphics.Rect
import android.text.TextUtils
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView

private const val FOCUSED = true
private const val SELECTED = true
private const val SINGLE_LINE = true
private const val MARQUE_FOREVER = -1

class RunningTextView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {

    init {
        isSingleLine = SINGLE_LINE
        marqueeRepeatLimit = MARQUE_FOREVER
        ellipsize = TextUtils.TruncateAt.MARQUEE
        isSelected = SELECTED
    }

    override fun onFocusChanged(focused: Boolean, direction: Int, previouslyFocusedRect: Rect?) {
        if (focused) super.onFocusChanged(focused, direction, previouslyFocusedRect)
    }

    override fun onWindowFocusChanged(hasWindowFocus: Boolean) {
        if (hasWindowFocus) super.onWindowFocusChanged(hasWindowFocus)
    }

    override fun isFocused(): Boolean = FOCUSED
    
}
