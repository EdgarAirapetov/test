package com.numplates.nomera3.modules.moments.show.presentation.custom

import android.view.View
import androidx.viewpager2.widget.ViewPager2

const val TRANSFORM_DEFAULT_PIVOT = 0f

private const val TRANSFORM_DEFAULT_SCALE = 1f
private const val TRANSFORM_DEFAULT_ROTATION = 0f
private const val TRANSFORM_DEFAULT_TRANSLATION = 0f
private const val TRANSFORM_MAX_ALPHA = 1f
private const val TRANSFORM_MIN_ALPHA = 0f

abstract class BaseTransform : ViewPager2.PageTransformer {

    protected open val isPagingEnabled: Boolean
        get() = false

    protected abstract fun onTransform(page: View, position: Float)

    override fun transformPage(page: View, position: Float) {
        val clampedPosition = clampPosition(position)
        onPreTransform(page, clampedPosition)
        onTransform(page, clampedPosition)
        onPostTransform(page, clampedPosition)
    }

    protected open fun hideOffscreenPages() = true

    protected open fun onPreTransform(page: View, position: Float) {
        page.apply {
            val width = width.toFloat()
            rotationX = TRANSFORM_DEFAULT_ROTATION
            rotationY = TRANSFORM_DEFAULT_ROTATION
            rotation = TRANSFORM_DEFAULT_ROTATION
            scaleX = TRANSFORM_DEFAULT_SCALE
            scaleY = TRANSFORM_DEFAULT_SCALE
            pivotX = TRANSFORM_DEFAULT_PIVOT
            pivotY = TRANSFORM_DEFAULT_PIVOT
            translationY = TRANSFORM_DEFAULT_TRANSLATION
            translationX = if (isPagingEnabled) TRANSFORM_DEFAULT_TRANSLATION else -width * position
            if (hideOffscreenPages()) {
                alpha = if (position <= -1f || position >= 1f) TRANSFORM_MIN_ALPHA else TRANSFORM_MAX_ALPHA
                isEnabled = false
            } else {
                isEnabled = true
                alpha = TRANSFORM_MAX_ALPHA
            }
        }
    }

    protected open fun onPostTransform(page: View, position: Float) {}

    private fun clampPosition(position: Float): Float {
        return when {
            position < -1f -> -1f
            position > 1f -> 1f
            position.isNaN() -> 0f
            else -> position
        }
    }
}
