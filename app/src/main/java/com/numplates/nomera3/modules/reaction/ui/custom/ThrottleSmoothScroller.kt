package com.numplates.nomera3.modules.reaction.ui.custom

import android.widget.HorizontalScrollView
import kotlin.math.abs

/**
 * Скролл HorizontalScrollView с троттлингом
 */
class ThrottleSmoothScroller(
    private val scrollView: HorizontalScrollView
) {
    private var lastScrollTime = 0L

    private val delayMillis: Long = 500L

    fun scroll(dx: Int) {
        val currentTimestamp: Long = System.currentTimeMillis()
        if(abs(currentTimestamp - lastScrollTime) > delayMillis) {
            lastScrollTime = currentTimestamp
            scrollView.smoothScrollBy(dx, 0)
        }
    }

}
