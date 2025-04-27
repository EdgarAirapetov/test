package com.numplates.nomera3.modules.viewvideo.presentation.view

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.google.android.exoplayer2.C
import com.meera.core.extensions.dp
import com.meera.core.extensions.invisible
import com.meera.core.extensions.visible
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.ViewVideoSeekBarBinding

private const val ANIMATE_VISIBILITY_DURATION = 100L

class ViewVideoSeekBarView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attrs, defStyle) {

    private val binding = LayoutInflater.from(context)
        .inflate(R.layout.view_video_seek_bar, this, false)
        .apply(::addView)
        .let(ViewVideoSeekBarBinding::bind)

    private val maxHeight = 12.dp
    private val minHeight = 2.dp

    private var currentDuration: Long = C.TIME_UNSET
    private var currentPosition: Long = C.TIME_UNSET

    private var visibilityAnimator: ValueAnimator? = null

    private var isShown: Boolean = false

    fun setDuration(duration: Long) {
        currentDuration = duration
    }

    fun setPosition(position: Long) {
        currentPosition = position
        renderCurrentPosition(position)
    }

    fun getPosition() = currentPosition

    fun show() {
        if (isShown) return
        isShown = true

        setHeight(height = minHeight)
        visible()

        visibilityAnimator = ValueAnimator.ofInt(minHeight, maxHeight).also { valueAnimator ->
            valueAnimator.apply {
                addUpdateListener { valueAnimator ->
                    val height = valueAnimator.animatedValue as Int
                    setHeight(height = height)

                    if (height == maxHeight) renderCurrentPosition(currentPosition)
                }
                duration = ANIMATE_VISIBILITY_DURATION
                start()
            }
        }
    }

    fun hide() {
        isShown = false
        cancelAnimation()

        setHeight(height = maxHeight)

        visibilityAnimator = ValueAnimator.ofInt(maxHeight, minHeight).also { valueAnimator ->
            valueAnimator.apply {
                addUpdateListener { valueAnimator ->
                    val height = valueAnimator.animatedValue as Int
                    setHeight(height = height)

                    if (height == minHeight) invisible()
                }
                duration = ANIMATE_VISIBILITY_DURATION
                start()
            }
        }
    }

    private fun cancelAnimation() {
        visibilityAnimator?.cancel()
        visibilityAnimator = null
    }

    private fun setHeight(height: Int) {
        val lp = layoutParams
        lp.height = height
        layoutParams = lp
    }

    private fun renderCurrentPosition(position: Long) {
        if (currentDuration == 0L) return
        val lp = binding.sivViewVideoSeekBar.layoutParams
        lp.width = ((binding.sivViewVideoSeekBarBackground.width * position) / currentDuration).toInt()
        binding.sivViewVideoSeekBar.layoutParams = lp
    }
}

fun ViewVideoSeekBarView.getCurrentPosition(
    positionX: Int,
    totalDuration: Long
): Long {
    val timeBarRect = Rect()
    this.getGlobalVisibleRect(timeBarRect)

    var actualX = positionX - timeBarRect.left
    val timeBarDistance = timeBarRect.right - timeBarRect.left

    when {
        actualX < 0f -> actualX = 0
        actualX > timeBarDistance -> actualX = timeBarDistance
    }

    return (actualX * totalDuration) / timeBarDistance
}
