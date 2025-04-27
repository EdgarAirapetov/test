package com.numplates.nomera3.modules.maps.ui.pin

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.constraintlayout.widget.ConstraintLayout
import com.meera.core.extensions.dp
import com.meera.core.extensions.gone
import com.meera.core.extensions.invisible
import com.meera.core.extensions.setBackgroundTint
import com.meera.core.extensions.setMargins
import com.meera.core.extensions.visible
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.ViewEventPinLargeBinding
import com.numplates.nomera3.modules.maps.ui.pin.model.EventLargePinUiModel
import com.numplates.nomera3.modules.maps.ui.pin.model.EventPinImage
import com.numplates.nomera3.modules.maps.ui.pin.model.PinView

class EventLargePinView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attrs, defStyle), PinView {

    private val binding: ViewEventPinLargeBinding = LayoutInflater.from(context)
        .inflate(R.layout.view_event_pin_large, this, false)
        .apply(::addView)
        .let(ViewEventPinLargeBinding::bind)

    override fun getAnchorX(): Float = binding.ivEventPinLargeStem.x / width

    override fun getAnchorY(): Float = (binding.ivEventPinLargeStem.y + binding.ivEventPinLargeStem.height) / height

    fun show(uiModel: EventLargePinUiModel) {
        when (uiModel.image) {
            EventPinImage.ImageError -> {
                binding.ivEventPinLargeImage.invisible()
                binding.ivEventPinLargeImagePlaceholder.visible()
                binding.tvEventPinLargeEventTitle.maxWidth = TEXT_MAX_WIDTH_IMAGE_DP.dp.toInt()
                binding.tvEventPinLargeEventTitle.setMargins(top = 0, bottom = 0)
            }
            is EventPinImage.ImageLoaded -> {
                binding.ivEventPinLargeImage.setImageBitmap(uiModel.image.bitmap)
                binding.ivEventPinLargeImage.visible()
                binding.ivEventPinLargeImagePlaceholder.gone()
                binding.tvEventPinLargeEventTitle.maxWidth = TEXT_MAX_WIDTH_IMAGE_DP.dp.toInt()
                binding.tvEventPinLargeEventTitle.setMargins(top = 0, bottom = 0)
            }
            EventPinImage.NoImage -> {
                binding.ivEventPinLargeImage.gone()
                binding.ivEventPinLargeImagePlaceholder.gone()
                binding.tvEventPinLargeEventTitle.maxWidth = TEXT_MAX_WIDTH_NO_IMAGE_DP.dp.toInt()
                setNoImageVerticalTextMargin(TEXT_SIZE_LARGE_NO_IMAGE_VERTICAL_MARGIN_DP.dp.toInt())
            }
        }
        binding.ivEventPinLargeEvent.setImageResource(uiModel.eventIconResId)
        binding.ivEventPinLargeEvent.setBackgroundTint(uiModel.eventColorResId)
        binding.tvEventPinLargeEventTitle.textSize = TEXT_SIZE_LARGE_SP
        binding.tvEventPinLargeEventTitle.text = uiModel.title
        if (adjustTextSize(uiModel)) {
            adjustTextWidth()
        }
    }

    private fun adjustTextSize(uiModel: EventLargePinUiModel): Boolean {
        measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED)
        return if (binding.tvEventPinLargeEventTitle.lineCount > 1) {
            if (uiModel.image == EventPinImage.NoImage) {
                setNoImageVerticalTextMargin(TEXT_SIZE_DEFAULT_NO_IMAGE_VERTICAL_MARGIN_DP.dp.toInt())
            }
            binding.tvEventPinLargeEventTitle.textSize = TEXT_SIZE_DEFAULT_SP
            true
        } else {
            false
        }
    }

    private fun adjustTextWidth() {
        measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED)
        val currentWidth = binding.tvEventPinLargeEventTitle.measuredWidth
        val maxLineWidth = (0 until binding.tvEventPinLargeEventTitle.lineCount)
            .maxOf { line -> binding.tvEventPinLargeEventTitle.layout.getLineWidth(line) }
            .toInt()
        if (currentWidth - maxLineWidth  > TEXT_WIDTH_DELTA_TOLERANCE_PX) {
            binding.tvEventPinLargeEventTitle.layoutParams.width = maxLineWidth + TEXT_WIDTH_DELTA_TOLERANCE_PX
        }
    }

    private fun setNoImageVerticalTextMargin(verticalMargin: Int) {
        (binding.tvEventPinLargeEventTitle.layoutParams as? ConstraintLayout.LayoutParams)
            ?.apply {
                goneTopMargin = verticalMargin
                goneBottomMargin = verticalMargin
            }
    }

    companion object {
        private const val TEXT_SIZE_DEFAULT_SP = 16f
        private const val TEXT_SIZE_LARGE_SP = 24f
        private const val TEXT_SIZE_LARGE_NO_IMAGE_VERTICAL_MARGIN_DP = 12f
        private const val TEXT_SIZE_DEFAULT_NO_IMAGE_VERTICAL_MARGIN_DP = 9f
        private const val TEXT_MAX_WIDTH_NO_IMAGE_DP = 216f
        private const val TEXT_MAX_WIDTH_IMAGE_DP = 168f
        private const val TEXT_WIDTH_DELTA_TOLERANCE_PX = 5
    }
}
