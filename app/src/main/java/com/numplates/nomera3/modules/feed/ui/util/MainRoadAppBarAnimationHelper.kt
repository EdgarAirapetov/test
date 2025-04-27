package com.numplates.nomera3.modules.feed.ui.util

import android.view.View
import android.view.ViewPropertyAnimator
import com.meera.core.extensions.dp

private const val ANIMATION_DURATION = 100L
private const val TRANSLATION_Y_VALUE = -45f

class MainRoadAppBarAnimationHelper {
    private var isAnimating = false

    private var tabsAnimator: ViewPropertyAnimator? = null
    private var smallTabsAnimator: ViewPropertyAnimator? = null

    private var tabs: View? = null
    private var smallTabs: View? = null

    fun startAnimate(tabs: View, smallTabs: View, shouldExpand: Boolean, onAnimationFinished: (Boolean) -> Unit) {
        this.tabs = tabs
        this.smallTabs = smallTabs

        tabsAnimator = tabs.animate()
            .setDuration(ANIMATION_DURATION)
            .translationY(if (!shouldExpand) (TRANSLATION_Y_VALUE.dp) else 0f)
            .withStartAction { isAnimating = true }.also { it.start() }

        smallTabsAnimator = smallTabs.animate()
            .setDuration(ANIMATION_DURATION)
            .translationY(if (!shouldExpand) (TRANSLATION_Y_VALUE.dp) else 0f)
            .withEndAction {
                isAnimating = false
                onAnimationFinished.invoke(shouldExpand)
            }.also { it.start() }
    }

    fun isAnimating() = isAnimating

    fun resetAnimations() {
        tabsAnimator?.cancel()
        smallTabsAnimator?.cancel()

        tabs?.translationY = 0f
        smallTabs?.translationY = 0f

        isAnimating = false
    }

    fun clear() {
        resetAnimations()
        tabsAnimator = null
        smallTabsAnimator = null

        tabs = null
        smallTabs = null
    }
}
