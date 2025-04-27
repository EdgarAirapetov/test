package com.numplates.nomera3.modules.moments.show.presentation.view

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import kotlin.math.min

/**
 * Aspect Ratio ViewGroup that doesn't force aspect ratio if [softSide] cannot fit in parent's bounds.
 */
class SoftAspectRatioFrameLayout @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAtr: Int = 0
) : FrameLayout(context, attributeSet, defStyleAtr) {

    private var aspectRatio: Float = RATIO_9_TO_16
    private var softSide: SoftSide = SoftSide.HEIGHT

    fun setAspectRatio(widthHeightRatio: Float) {
        if (aspectRatio != widthHeightRatio) {
            aspectRatio = widthHeightRatio
            requestLayout()
        }
    }

    fun setSoftOrientation(orientation: SoftSide) {
        if (softSide != orientation) {
            softSide = orientation
            requestLayout()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val (frameWidth, frameHeight) = when (softSide) {
            SoftSide.HEIGHT -> {
                val strictWidth = MeasureSpec.getSize(widthMeasureSpec)
                val limitHeight = MeasureSpec.getSize(heightMeasureSpec)
                val ratioHeight = (strictWidth / aspectRatio).toInt()
                strictWidth to min(limitHeight, ratioHeight)
            }
            SoftSide.WIDTH -> {
                val strictHeight = MeasureSpec.getSize(heightMeasureSpec)
                val limitWidth = MeasureSpec.getSize(widthMeasureSpec)
                val ratioWidth = (strictHeight * aspectRatio).toInt()
                min(limitWidth, ratioWidth) to strictHeight
            }
        }
        super.onMeasure(
            MeasureSpec.makeMeasureSpec(frameWidth, MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec(frameHeight, MeasureSpec.EXACTLY)
        )
    }

    companion object {
        const val RATIO_9_TO_16 = 9f / 16f
        const val RATIO_16_TO_9 = 16f / 9f
        const val RATIO_3_TO_4 = 3f / 4f
        const val RATIO_4_TO_3 = 4f / 3f
    }
}

/**
 * Soft side of the ViewGroup, constrained(if needed) to the parent's provided bounds
 */
enum class SoftSide {
    WIDTH, HEIGHT
}
