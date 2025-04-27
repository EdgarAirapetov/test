package com.numplates.nomera3.modules.maps.ui.events

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.TimePicker

class CustomTimePicker@JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : TimePicker(context, attrs) {

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> parent.requestDisallowInterceptTouchEvent(true)
            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> parent.requestDisallowInterceptTouchEvent(false)
            else -> Unit
        }
        return super.onInterceptTouchEvent(event)
    }
}
