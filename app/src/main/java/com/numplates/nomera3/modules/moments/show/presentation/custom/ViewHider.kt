package com.numplates.nomera3.modules.moments.show.presentation.custom

import android.animation.ObjectAnimator
import android.view.View
import androidx.core.animation.doOnEnd
import androidx.core.view.isGone
import androidx.core.view.isInvisible

private const val ALPHA_PROPERTY = "alpha"
private const val HIDE_DURATION = 500L

class ViewHider {

    private val viewsList = mutableListOf<ViewHiderItem>()
    private var isViewsHidden = false

    fun hideViews(views: List<View>, useAnimation: Boolean = false) {
        fillList(views)
        isViewsHidden = true
        viewsList.filter { !it.isInvisible && !it.isGone }
            .forEach {
                it.isHidden = true
                if (useAnimation) {
                    it.startHideAnimation()
                } else {
                    it.view.toggleAvailabilityAndVisibility(false)
                }
            }
    }

    fun showViews() {
        if (!isViewsHidden) return
        viewsList.filter { !it.isInvisible && !it.isGone }
            .forEach {
                it.isHidden = false
                it.view.toggleAvailabilityAndVisibility(true)
            }
        viewsList.clear()
        isViewsHidden = false
    }

    private fun View.toggleAvailabilityAndVisibility(isVisibleAndAvailable: Boolean) {
        isInvisible = !isVisibleAndAvailable
        isClickable = isVisibleAndAvailable
        isFocusable = isVisibleAndAvailable
    }

    private fun ViewHiderItem.startHideAnimation() {
        view.playHideAnimation(HIDE_DURATION) { view ->
            if (isViewsHidden) {
                view.toggleAvailabilityAndVisibility(false)
            } else {
                isHidden = false
                view.toggleAvailabilityAndVisibility(true)
            }
            view.alpha = 1f
        }
    }

    private fun View.playHideAnimation(
        durationMs: Long,
        completeCallback: ((View) -> Unit)? = null
    ) {
        ObjectAnimator.ofFloat(this, ALPHA_PROPERTY, 1f, 0f).apply {
            duration = durationMs
            doOnEnd { completeCallback?.invoke(this@playHideAnimation) }
        }.start()
    }

    private fun fillList(views: List<View>) {
        views.forEach {
            viewsList.add(
                ViewHiderItem(view = it, isInvisible = it.isInvisible, isGone = it.isGone)
            )
        }
    }
}

data class ViewHiderItem(
    val view: View,
    var isInvisible: Boolean = false,
    var isGone: Boolean = false,
    var isHidden: Boolean = false
)
