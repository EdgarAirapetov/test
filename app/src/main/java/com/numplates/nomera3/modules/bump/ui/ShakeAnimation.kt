package com.numplates.nomera3.modules.bump.ui

import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.AnimationSet
import android.view.animation.TranslateAnimation
import com.meera.core.extensions.addAnimationListener

private const val TRANSLATE_VIEW_DURATION = 400L
private const val ALPHA_VIEW_DURATION = 400L
private const val TRANSLATE_TO_X_DELTA = 100F
private const val TRANSLATE_TO_Y_DELTA = -100f

internal inline fun View.animateNextUser(
    crossinline animationEndListener: (() -> Unit) = {  }
) {
    val set = AnimationSet(true)
    val trAnimation: Animation = TranslateAnimation(
        0f,
        TRANSLATE_TO_X_DELTA,
        0f,
        0f
    )
    trAnimation.duration = TRANSLATE_VIEW_DURATION
    trAnimation.repeatMode = Animation.REVERSE
    set.addAnimation(trAnimation)
    val anim: Animation = AlphaAnimation(1.0f, 0.0f)
    anim.duration = ALPHA_VIEW_DURATION
    set.addAnimation(anim)
    this.startAnimation(set)
    set.addAnimationListener(animationEndListener = {
        animationEndListener.invoke()
    })
}

internal inline fun View.animateSkipUser(
    crossinline animationEndListener: (() -> Unit) = {  }
) {
    val set = AnimationSet(true)
    val trAnimation: Animation = TranslateAnimation(
        0f,
        0f,
        0f,
        TRANSLATE_TO_Y_DELTA
    )
    trAnimation.duration = TRANSLATE_VIEW_DURATION
    trAnimation.repeatMode = Animation.REVERSE
    set.addAnimation(trAnimation)
    val anim: Animation = AlphaAnimation(1.0f, 0.0f)
    anim.duration = ALPHA_VIEW_DURATION
    set.addAnimation(anim)
    this.startAnimation(set)
    set.addAnimationListener(animationEndListener = {
        animationEndListener.invoke()
    })
}
