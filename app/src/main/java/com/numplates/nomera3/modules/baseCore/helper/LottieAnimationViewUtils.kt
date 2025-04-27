package com.numplates.nomera3.modules.baseCore.helper

import android.animation.Animator
import com.airbnb.lottie.LottieAnimationView
import com.meera.core.extensions.invisibleAnimation
import com.meera.core.extensions.visibleAnimation

private const val FADE_IN_DURATION = 300

fun LottieAnimationView.hide() = invisibleAnimation()

fun LottieAnimationView.show() = visibleAnimation(FADE_IN_DURATION)

fun LottieAnimationView.addAnimationListener(
    onStart: LottieAnimationView.() -> Unit = {},
    onEnd: LottieAnimationView.() -> Unit = {},
    onCancel: LottieAnimationView.() -> Unit = {},
    onRepeat: LottieAnimationView.() -> Unit = {}
) {
    addAnimatorListener(
        object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {
                onStart.invoke(this@addAnimationListener)
            }

            override fun onAnimationEnd(animation: Animator) {
                onEnd.invoke(this@addAnimationListener)
            }

            override fun onAnimationCancel(animation: Animator) {
                onCancel.invoke(this@addAnimationListener)
            }

            override fun onAnimationRepeat(animation: Animator) {
                onRepeat.invoke(this@addAnimationListener)
            }
        }
    )
}
