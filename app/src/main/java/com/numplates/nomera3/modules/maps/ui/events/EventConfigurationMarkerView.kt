package com.numplates.nomera3.modules.maps.ui.events

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Point
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.meera.core.extensions.animateHeight
import com.meera.core.extensions.animateWidth
import com.meera.core.extensions.dp
import com.meera.core.extensions.getXRelativeToParent
import com.meera.core.extensions.getYRelativeToParent
import com.meera.core.extensions.setMargins
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.ViewEventConfigurationMarkerBinding
import com.numplates.nomera3.modules.maps.ui.events.model.EventConfigurationMarkerState

class EventConfigurationMarkerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attrs, defStyle) {

    private var onAddressClick: ((String) -> Unit)? = null

    private val binding = LayoutInflater.from(context)
        .inflate(R.layout.view_event_configuration_marker, this, false)
        .apply(::addView)
        .let(ViewEventConfigurationMarkerBinding::bind)

    private var state: EventConfigurationMarkerState = EventConfigurationMarkerState.Default

    init {
        binding.tvEventConfigurationMarkerAddress.setOnClickListener {
            onAddressClick?.invoke(binding.tvEventConfigurationMarkerAddress.text.toString())
        }
        setState(EventConfigurationMarkerState.Progress(false))
    }

    fun setOnAddressClickListener(onAddressClick: (String) -> Unit) {
        this.onAddressClick = onAddressClick
    }

    fun setState(newState: EventConfigurationMarkerState) {
        if (newState == state) return

        binding.pbEventConfigurationMarkerProgress.isVisible =
            newState is EventConfigurationMarkerState.Progress || newState == EventConfigurationMarkerState.Error
        val progressColorResId = if (newState == EventConfigurationMarkerState.Error) {
            R.color.colorGrayA7A5
        } else {
            R.color.ui_purple
        }
        binding.pbEventConfigurationMarkerProgress.indeterminateDrawable
            .setTint(ContextCompat.getColor(context, progressColorResId))
        if (state.isLevitating != newState.isLevitating) {
            if (newState.isLevitating) {
                animateLevitation()
            } else {
                animateLanding()
            }
        }
        binding.tvEventConfigurationMarkerAddress.text = (newState as? EventConfigurationMarkerState.Address)
            ?.markerAddress
        binding.ivEventConfigurationMarkerArrow.isVisible = newState is EventConfigurationMarkerState.Address
        val textWidth = getAdjustedTextWidth()
        binding.tvEventConfigurationMarkerAddress.layoutParams.width = textWidth
        animateTextSizeChange(
            targetWidth = textWidth,
            targetHeight = binding.tvEventConfigurationMarkerAddress.measuredHeight
        )
        state = newState
    }

    fun getTipPositionRelative(parentView: View): Point {
        return Point(
            (getXRelativeToParent(parentView) + width).toInt() / 2,
            (getYRelativeToParent(parentView) + height).toInt() - binding.ivEventConfigurationMarkerShadow.height / 2
        )
    }

    private fun getAdjustedTextWidth(): Int {
        val widthMeasureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
        val heightMeasureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
        binding.tvEventConfigurationMarkerAddress.measure(widthMeasureSpec, heightMeasureSpec)
        val currentWidth = binding.tvEventConfigurationMarkerAddress.measuredWidth
        val maxLineWidth = (0 until binding.tvEventConfigurationMarkerAddress.lineCount)
            .maxOf { line -> binding.tvEventConfigurationMarkerAddress.layout.getLineWidth(line) }
            .toInt()
        return if (currentWidth - maxLineWidth > TEXT_WIDTH_DELTA_TOLERANCE_PX) {
            maxLineWidth + TEXT_WIDTH_DELTA_TOLERANCE_PX
        } else {
            currentWidth
        }
    }

    private fun animateTextSizeChange(targetWidth: Int, targetHeight: Int) {
        binding.tvEventConfigurationMarkerAddress.animateWidth(targetWidth, ANIMATION_DURATION_MS)
        binding.tvEventConfigurationMarkerAddress.animateHeight(targetHeight, ANIMATION_DURATION_MS)
    }

    private fun animateLevitation() {
        val valueAnimator = ValueAnimator.ofInt(TIP_DEFAULT_Y_OFFSET_PX, TIP_LEVITATION_Y_OFFSET_PX)
        valueAnimator.addUpdateListener { animator ->
            binding.ivEventConfigurationMarkerShadow.setMargins(top = animator.animatedValue as Int)
        }
        valueAnimator.interpolator = LinearInterpolator()
        valueAnimator.duration = ANIMATION_DURATION_MS
        valueAnimator.start()
    }

    private fun animateLanding() {
        val valueAnimator = ValueAnimator.ofInt(TIP_LEVITATION_Y_OFFSET_PX, TIP_DEFAULT_Y_OFFSET_PX)
        valueAnimator.addUpdateListener { animator ->
            binding.ivEventConfigurationMarkerShadow.setMargins(top = animator.animatedValue as Int)
        }
        valueAnimator.interpolator = LinearInterpolator()
        valueAnimator.duration = ANIMATION_DURATION_MS
        valueAnimator.start()
    }

    companion object {
        private const val ANIMATION_DURATION_MS = 200L
        private val TIP_LEVITATION_Y_OFFSET_PX = 12.dp
        private val TIP_DEFAULT_Y_OFFSET_PX = (-5).dp
        private const val TEXT_WIDTH_DELTA_TOLERANCE_PX = 5
    }
}
