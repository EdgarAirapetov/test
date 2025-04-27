package com.numplates.nomera3.modules.baseCore.helper

import android.animation.Animator
import androidx.fragment.app.Fragment
import com.meera.core.extensions.doDelayed

class DelayedAnimationRepeater(
    private val fragment: Fragment,
    private val delayMillis: Long
) : Animator.AnimatorListener {

    override fun onAnimationStart(animation: Animator) = Unit

    override fun onAnimationEnd(animation: Animator) = Unit

    override fun onAnimationCancel(animation: Animator) = Unit

    override fun onAnimationRepeat(animation: Animator) {
        animation?.pause()
        fragment.lifecycle.doDelayed(delayMillis) {
            animation?.resume()
        }
    }

}
