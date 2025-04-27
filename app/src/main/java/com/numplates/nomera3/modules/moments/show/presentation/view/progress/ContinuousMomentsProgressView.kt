package com.numplates.nomera3.modules.moments.show.presentation.view.progress

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.animation.LinearInterpolator

private const val LOAD_LINE_DEFAULT_BAR_TIME_LENGTH_MS = 10_000L

/**
 * Progress View for moment cards.
 *
 * Progress of the current bar is continually updated depending on the [barTimeLengthMs].
 *
 * Managed with [start], [resume], [pause].
 *
 * Used with [onProgressEndedCallback]
 */
class ContinuousMomentsProgressView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : BaseMomentsProgressView(context, attrs, defStyleAttr) {

    private var barTimeLengthMs: Long = LOAD_LINE_DEFAULT_BAR_TIME_LENGTH_MS
    private var valueAnimator: ValueAnimator? = null
    private val updateListener = UpdateListener()
    private val animationListener = AnimationListener()
    private var onProgressEndedCallback: ((progressBar: Int) -> Unit)? = null

    fun setOnProgressEndedCallback(callback: ((progressBar: Int) -> Unit)?) {
        onProgressEndedCallback = callback
    }

    fun initCurrentProgress(progressBar: Int) {
        resetProgressData()
        currentBar = progressBar
        valueAnimator = ValueAnimator
            .ofFloat(0f, 1f)
            .apply {
                interpolator = LinearInterpolator()
                addUpdateListener(updateListener)
                addListener(animationListener)
            }
    }

    fun start(barDuration: Long?) {
        valueAnimator?.duration = barDuration ?: barTimeLengthMs
        valueAnimator?.start()
    }

    fun pause() {
        valueAnimator?.pause()
    }

    fun pause(pausedAt: Long?) {
        pause()
        pausedAt?.let { valueAnimator?.currentPlayTime = pausedAt }
    }

    fun resume() {
        valueAnimator?.resume()
    }

    private fun resetProgressData() {
        clearValueAnimator()
        currentBar = 0
        currentBarProgress = 0f
    }

    private fun clearValueAnimator() {
        valueAnimator?.removeAllUpdateListeners()
        valueAnimator?.removeAllListeners()
        valueAnimator?.cancel()
        valueAnimator = null
    }

    private inner class UpdateListener : ValueAnimator.AnimatorUpdateListener {
        override fun onAnimationUpdate(animation: ValueAnimator) {
            currentBarProgress = animation.animatedFraction
            invalidate()
        }
    }

    private inner class AnimationListener : AnimatorListenerAdapter() {

        var isCanceled = false

        override fun onAnimationStart(animation: Animator) {
            isCanceled = false
        }

        override fun onAnimationCancel(animation: Animator) {
            isCanceled = true
        }

        override fun onAnimationEnd(animation: Animator) {
            if (isCanceled) return
            onProgressEndedCallback?.invoke(currentBar)
        }
    }
}
