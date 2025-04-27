package com.numplates.nomera3.presentation.view.ui.mediaViewer.viewer.view

import android.view.Gravity
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ImageView.ScaleType.CENTER_CROP
import android.widget.ImageView.ScaleType.FIT_CENTER
import androidx.transition.AutoTransition
import androidx.transition.ChangeImageTransform
import androidx.transition.TransitionManager
import androidx.transition.TransitionSet
import com.meera.core.extensions.invisible
import com.meera.core.extensions.setMargins
import com.meera.core.extensions.visible
import com.numplates.nomera3.presentation.view.ui.mediaViewer.common.extensions.globalVisibleRect
import com.numplates.nomera3.presentation.view.ui.mediaViewer.common.extensions.isRectVisible
import com.numplates.nomera3.presentation.view.ui.mediaViewer.common.extensions.localVisibleRect
import com.numplates.nomera3.presentation.view.ui.mediaViewer.common.extensions.postApply
import com.meera.core.extensions.addListener
import com.meera.core.extensions.doDelayed
import com.meera.core.extensions.newSize


internal class TransitionImageAnimator(
        private val externalImage: ImageView?,
        private val internalImage: ImageView?,
        private val internalImageContainer: FrameLayout?
) {

    companion object {
        private const val TRANSITION_DURATION_OPEN = 300L
        private const val TRANSITION_DURATION_CLOSE = 280L
    }

    internal var isAnimating = false

    private var isClosing = false

    private val transitionDuration: Long
        get() = if (isClosing) TRANSITION_DURATION_CLOSE else TRANSITION_DURATION_OPEN

    private val internalRoot: ViewGroup?
        get() = internalImageContainer?.parent as ViewGroup

    internal fun animateOpen(
            onTransitionStart: (Long) -> Unit,
            onTransitionEnd: () -> Unit
    ) {
        if (externalImage.isRectVisible) {
            onTransitionStart(TRANSITION_DURATION_OPEN)
            doOpenTransition(onTransitionEnd)
        } else {
            onTransitionEnd()
        }
    }

    internal fun animateClose(
            shouldDismissToBottom: Boolean,
            onTransitionStart: (Long) -> Unit,
            onTransitionEnd: () -> Unit
    ) {
        if (externalImage.isRectVisible && !shouldDismissToBottom) {
            onTransitionStart(TRANSITION_DURATION_CLOSE)
            doCloseTransition(onTransitionEnd)
        } else {
            externalImage?.visible()
            onTransitionEnd()
        }
    }

    private inline fun doOpenTransition(crossinline onTransitionEnd: () -> Unit) {

        isAnimating = true


        prepareTransitionLayout()

        internalRoot?.postApply {

            doDelayed(10) {
                externalImage?.invisible()
            }

            internalImage?.scaleType = CENTER_CROP
            internalImageContainer?.setMargins(0, 0, 0, 0)
            internalImageContainer?.newSize(MATCH_PARENT, MATCH_PARENT)

            TransitionManager.beginDelayedTransition(internalRoot!!, createTransition {
                if (!isClosing) {
                    isAnimating = false
                    onTransitionEnd()
                }
            })

            internalImage?.scaleType = FIT_CENTER
            internalImage?.let {
                val params = internalImage.layoutParams as FrameLayout.LayoutParams
                params.width = MATCH_PARENT
                params.height = MATCH_PARENT
                params.gravity = Gravity.CENTER
                internalImage.layoutParams = params
            }
        }
    }

    private fun doCloseTransition(onTransitionEnd: () -> Unit) {
        isAnimating = true
        isClosing = true

        internalRoot?.let {
            TransitionManager.beginDelayedTransition(
                    it, createTransition { handleCloseTransitionEnd(onTransitionEnd) })
        }
        internalImage?.scaleType = CENTER_CROP
        prepareTransitionLayout()
//        internalImageContainer?.requestLayout()
    }

    private fun prepareTransitionLayout() {
        externalImage?.let {
            if (externalImage.isRectVisible) {
                with(externalImage.localVisibleRect) {
                    internalImage?.newSize(it.width, it.height)
                    internalImage?.setMargins(top = -top, start = -left)
                }
                with(externalImage.globalVisibleRect) {
                    internalImageContainer?.newSize(width(), height())
                    internalImageContainer?.setMargins(left, top, right, bottom)
                }
            }
            resetRootTranslation()
        }
    }

    private fun handleCloseTransitionEnd(onTransitionEnd: () -> Unit) {
        externalImage?.visible()
        externalImage?.doDelayed(50) {

            internalImage?.post { onTransitionEnd() }
            isAnimating = false
        }
        externalImage?.doDelayed(300) {
            externalImage.requestLayout()
        }
    }

    private fun resetRootTranslation() {
        internalRoot
                ?.animate()
                ?.translationY(0f)
                ?.setDuration(transitionDuration)
                ?.start()
    }

    val interpolator = DecelerateInterpolator()

    private fun createTransition(onTransitionEnd: (() -> Unit)? = null): TransitionSet =
            TransitionSet().addTransition(
                    AutoTransition()
                            .setDuration(transitionDuration)
                            .setInterpolator(interpolator))
                    .addTransition(
                            ChangeImageTransform()
                                    .setDuration(transitionDuration)
                                    .setInterpolator(interpolator)
                                    .addListener(onTransitionEnd = { onTransitionEnd?.invoke() }))

}
