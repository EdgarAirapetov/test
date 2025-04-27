package com.numplates.nomera3.modules.chat.helpers

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.recyclerview.widget.RecyclerView

class ChatImagesRecycler @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0,
) : RecyclerView(context, attrs ,defStyleAttr) {

    override fun onTouchEvent(e: MotionEvent?): Boolean {
        super.onTouchEvent(e)
        return false
    }

    override fun onInterceptTouchEvent(e: MotionEvent?): Boolean = false

}