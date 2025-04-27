package com.numplates.nomera3.presentation.view.widgets.anim

import android.view.animation.Animation

abstract class AnimationEndListener: Animation.AnimationListener {
    override fun onAnimationStart(animation: Animation?) = Unit

    override fun onAnimationRepeat(animation: Animation?) = Unit
}
