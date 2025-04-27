package com.numplates.nomera3.modules.reaction.ui.custom

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.HorizontalScrollView

class HorizontalBlockedScroll @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAtr: Int = 0
) : HorizontalScrollView(context, attributeSet, defStyleAtr) {

    override fun onTouchEvent(ev: MotionEvent?): Boolean {
        return true
    }

    fun bypassTouchEventThroughBlock(ev: MotionEvent?) {
        super.onTouchEvent(ev)
    }
}
