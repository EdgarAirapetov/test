package com.meera.core.utils.transition

import android.graphics.drawable.Drawable
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory
import com.bumptech.glide.request.transition.Transition
import com.bumptech.glide.request.transition.TransitionFactory

class PaddingTransitionFactory<T : Drawable>(
    private val realFactory: DrawableCrossFadeFactory
) : TransitionFactory<T> {

    override fun build(dataSource: DataSource, b: Boolean): Transition<T> {
        return PaddingTransition(realFactory.build(dataSource, b))
    }
}
