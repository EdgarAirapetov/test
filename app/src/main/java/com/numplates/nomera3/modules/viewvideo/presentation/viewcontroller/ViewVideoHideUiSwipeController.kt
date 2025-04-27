package com.numplates.nomera3.modules.viewvideo.presentation.viewcontroller

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.view.View
import android.view.ViewGroup
import android.view.ViewPropertyAnimator
import android.view.animation.AccelerateInterpolator
import androidx.dynamicanimation.animation.DynamicAnimation
import androidx.dynamicanimation.animation.FlingAnimation
import com.meera.core.extensions.gone
import com.meera.core.extensions.visible
import kotlin.math.abs
import kotlin.math.roundToLong

private const val DEFAULT_FLING_ANIMATION_FRICTION = 4.2f
private const val MILLISECONDS_IN_SECOND = 1_000
private const val MAX_TRANSLATION = 0f

class ViewVideoHideUiSwipeController(
    private val toolbarContainer: ViewGroup,
    private val swipeContainer: ViewGroup,
    private val bottomShadow: View,
    private val onCloseAction: () -> Unit
) {

    private val settlingAnimationDuration =
        swipeContainer.context.resources.getInteger(android.R.integer.config_shortAnimTime).toLong()
    private val accelInterpolator = AccelerateInterpolator()

    /**
     * How much the view must be translated by to be considered dismissed
     */
    private var dismissedTranslationRatio = -0.40f

    private var flingAnimation: FlingAnimation? = null

    private var translation = 0f
    private var startingTranslation = 0f
    private var isStartingTranslationSet = false

    fun onHorizontalSwipe(distanceX: Float) {
        swipeContainer.animate().cancel()
        flingAnimation?.cancel()
        swipeContainer.visible()
        if (isStartingTranslationSet.not()) {
            startingTranslation = swipeContainer.translationX
            isStartingTranslationSet = true
        }
        translation += distanceX
        swipeContainer.translationX = (startingTranslation + translation).coerceIn(getMinTranslationHorizontal(), MAX_TRANSLATION)
        setAlpha()
    }

    fun onHorizontalFling(velocityX: Float, onlyCloseSwipe: Boolean) {
        if (swipeContainer.translationX == getMinTranslationHorizontal() && velocityX < 0) return
        if (swipeContainer.translationX == MAX_TRANSLATION && velocityX > 0) {
            onCloseAction.invoke()
            return
        }
        if (onlyCloseSwipe) return
        handleRightSwipe(velocityX)
    }

    fun onVerticalFling() {
        if (swipeContainer.translationY == getMinTranslationVertical()) return
        if (swipeContainer.translationY == MAX_TRANSLATION) {
            onCloseAction.invoke()
        }
    }

    private fun handleRightSwipe(velocityX: Float) {
        resetSwipeData()
        flingAnimation = FlingAnimation(swipeContainer, DynamicAnimation.TRANSLATION_X).apply {
            setStartVelocity(velocityX)
            setMaxValue(MAX_TRANSLATION)
            setMinValue(getMinTranslationHorizontal())
            friction = DEFAULT_FLING_ANIMATION_FRICTION
            addUpdateListener { _, translation, velocity ->
                setAlpha()
                when {
                    velocityX < 0f && checkIfCanDismiss(translation) -> {
                        flingAnimation?.cancel()
                        animateToHide(velocity)
                    }
                    velocityX > 0f && checkIfCanShow(translation) -> {
                        flingAnimation?.cancel()
                        animateToShow(velocity)
                    }
                }
            }
            addEndListener { _, canceled, _, _ ->
                if (canceled.not()) {
                    onHorizontalSwipeEnded()
                }
            }
            start()
        }
    }

    fun onHorizontalSwipeEnded() {
        resetSwipeData()
        if (checkIfCanDismiss(swipeContainer.translationX)) {
            animateToHide()
        } else {
            animateToShow()
        }
    }

    private fun resetSwipeData() {
        translation = 0f
        isStartingTranslationSet = false
        startingTranslation = 0f
    }

    private fun animateToHide(velocityX: Float? = null) {
        swipeContainer.animateTranslationOffscreen(velocityX)
        toolbarContainer.animateAlphaTo(0f)
        bottomShadow.animateAlphaTo(0f)
    }

    private fun animateToShow(velocityX: Float? = null) {
        swipeContainer.animateTranslationOnscreen(velocityX)
        toolbarContainer.animateAlphaTo(1f)
        bottomShadow.animateAlphaTo(1f)
    }

    private fun setAlpha() {
        val fraction = (1 + (swipeContainer.translationX / swipeContainer.width)).coerceIn(0f, 1f)
        toolbarContainer.alpha = fraction
        bottomShadow.alpha = fraction
    }

    private fun checkIfCanDismiss(currentTranslation: Float): Boolean {
        return currentTranslation < dismissTranslation()
    }

    private fun checkIfCanShow(currentTranslation: Float): Boolean {
        return currentTranslation > dismissTranslation()
    }

    private fun dismissTranslation(): Float = swipeContainer.width * dismissedTranslationRatio

    private fun getMinTranslationHorizontal(): Float = -swipeContainer.width.toFloat()

    private fun getMinTranslationVertical(): Float = -swipeContainer.height.toFloat()

    private fun View.animateTranslationOffscreen(velocityX: Float?) {
        val duration = if (velocityX == null) settlingAnimationDuration else getFlingSettleAnimDuration(velocityX)
        animate()
            .setInterpolator(accelInterpolator)
            .translationX(getMinTranslationHorizontal())
            .setDuration(duration)
            .setEndListenerWithCleanUp {
                swipeContainer.gone()
            }
            .start()
    }

    private fun View.animateTranslationOnscreen(velocityX: Float?) {
        val duration = if (velocityX == null) settlingAnimationDuration else getFlingSettleAnimDuration(velocityX)
        animate()
            .setInterpolator(accelInterpolator)
            .translationX(MAX_TRANSLATION)
            .setDuration(duration)
            .start()
    }

    private fun View.animateAlphaTo(alpha: Float) {
        animate()
            .setInterpolator(accelInterpolator)
            .alpha(alpha)
            .setDuration(settlingAnimationDuration)
            .start()
    }

    private fun View.getFlingSettleAnimDuration(velocityX: Float): Long {
        return when {
            velocityX < 0f -> {
                ((swipeContainer.width - abs(translationX)) * MILLISECONDS_IN_SECOND / abs(velocityX))
                    .roundToLong()
                    .coerceAtMost(settlingAnimationDuration)
            }
            velocityX > 0f -> {
                (abs(translationX) * MILLISECONDS_IN_SECOND / velocityX)
                    .roundToLong()
                    .coerceAtMost(settlingAnimationDuration)
            }
            else -> settlingAnimationDuration
        }
    }

    private fun ViewPropertyAnimator.setEndListenerWithCleanUp(
        onAnimationEnd: ((Animator?) -> Unit)? = null
    ) = this.setListener(
        object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(p0: Animator) {
                onAnimationEnd?.invoke(p0)
                setListener(null)
            }

            override fun onAnimationCancel(p0: Animator) {
                setListener(null)
            }
        })

}
