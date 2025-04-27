package com.meera.core.extensions

import android.animation.Animator
import android.view.ViewPropertyAnimator
import androidx.transition.Transition

fun ViewPropertyAnimator.setListener(
    onAnimationEnd: ((Animator?) -> Unit)? = null,
    onAnimationRepeat: ((Animator?) -> Unit)? = null,
    onAnimationCancel: ((Animator?) -> Unit)? = null,
    onAnimationStart: ((Animator?) -> Unit)? = null
) = this.setListener(
    object : Animator.AnimatorListener {
        override fun onAnimationRepeat(p0: Animator) {
            onAnimationRepeat?.invoke(p0)
        }

        override fun onAnimationEnd(p0: Animator) {
            onAnimationEnd?.invoke(p0)
        }

        override fun onAnimationCancel(p0: Animator) {
            onAnimationCancel?.invoke(p0)
            setListener(null)
        }

        override fun onAnimationStart(p0: Animator) {
            onAnimationStart?.invoke(p0)
        }
    })

fun Transition.addListener(
    onTransitionEnd: ((Transition) -> Unit)? = null,
    onTransitionResume: ((Transition) -> Unit)? = null,
    onTransitionPause: ((Transition) -> Unit)? = null,
    onTransitionCancel: ((Transition) -> Unit)? = null,
    onTransitionStart: ((Transition) -> Unit)? = null
) = addListener(
    object : Transition.TransitionListener {
        override fun onTransitionEnd(transition: Transition) {
            onTransitionEnd?.invoke(transition)
        }

        override fun onTransitionResume(transition: Transition) {
            onTransitionResume?.invoke(transition)
        }

        override fun onTransitionPause(transition: Transition) {
            onTransitionPause?.invoke(transition)
        }

        override fun onTransitionCancel(transition: Transition) {
            onTransitionCancel?.invoke(transition)
        }

        override fun onTransitionStart(transition: Transition) {
            onTransitionStart?.invoke(transition)
        }
    })
