package com.meera.core.utils.transition

import android.graphics.drawable.Drawable
import com.bumptech.glide.request.transition.Transition

class PaddingTransition<T : Drawable>(private val realTransition: Transition<in T>) : Transition<T> {

    override fun transition(current: T, adapter: Transition.ViewAdapter): Boolean {
        val width = current.intrinsicWidth
        val height = current.intrinsicHeight
        return realTransition.transition(current, PaddingViewAdapter(adapter, width, height))
    }
}
