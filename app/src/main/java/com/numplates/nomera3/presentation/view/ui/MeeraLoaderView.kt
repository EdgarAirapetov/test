package com.numplates.nomera3.presentation.view.ui

import android.animation.Animator
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewPropertyAnimator
import android.widget.FrameLayout
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraViewLoaderBinding

const val LOADER_ANIMATION_DURATION = 200L

private const val START_SCALE_VALUE = 0f
private const val END_SCALE_VALUE = 1f

private const val START_ALPHA_VALUE = 0f
private const val END_ALPHA_VALUE = 1f

class MeeraLoaderView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attrs, defStyle) {

    private val binding = LayoutInflater.from(context)
        .inflate(R.layout.meera_view_loader, this, false)
        .apply(::addView)
        .let(MeeraViewLoaderBinding::bind)

    private var animation: ViewPropertyAnimator? = null

    fun show() {
        animation?.cancel()
        prepareAnimationToShow()
        animation?.start()
    }

    fun hide(onFinished: (() -> Unit)? = null) {
        cancelLottieAnimation()
        animation?.cancel()
        prepareAnimationToHide(onFinished)
        animation?.start()
    }

    private fun startLottieAnimation() {
        binding.lavLoaderView.takeIf { it.isAnimating.not() }?.apply {
            playAnimation()
        }
    }

    private fun cancelLottieAnimation() {
        binding.lavLoaderView.takeIf { it.isAnimating }?.apply {
            cancelAnimation()
        }
    }

    private fun prepareAnimationToShow() {
        binding.lavLoaderView.apply {
            alpha = START_ALPHA_VALUE
            scaleX = START_SCALE_VALUE
            scaleY = START_SCALE_VALUE
        }

        animation = binding.lavLoaderView.animate()
            .alpha(END_ALPHA_VALUE)
            .scaleX(END_SCALE_VALUE)
            .scaleY(END_SCALE_VALUE)
            .setListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator) = Unit
                override fun onAnimationCancel(animation: Animator) = Unit
                override fun onAnimationRepeat(animation: Animator) = Unit
                override fun onAnimationEnd(animation: Animator) { startLottieAnimation() }
            })
            .setDuration(LOADER_ANIMATION_DURATION)
    }

    private fun prepareAnimationToHide(onFinished: (() -> Unit)?) {
        binding.lavLoaderView.apply {
            alpha = END_ALPHA_VALUE
            scaleX = END_ALPHA_VALUE
            scaleY = END_ALPHA_VALUE
        }

        animation = binding.lavLoaderView.animate()
            .alpha(START_ALPHA_VALUE)
            .scaleX(START_SCALE_VALUE)
            .scaleY(START_SCALE_VALUE)
            .setListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator) = Unit
                override fun onAnimationCancel(animation: Animator) = Unit
                override fun onAnimationRepeat(animation: Animator) = Unit
                override fun onAnimationEnd(animation: Animator) { onFinished?.invoke() }
            })
            .setDuration(LOADER_ANIMATION_DURATION)
    }
}
