package com.numplates.nomera3.modules.maps.ui.pin

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.ViewEventPinSmallBinding
import com.numplates.nomera3.modules.maps.ui.pin.model.EventSmallPinUiModel
import com.numplates.nomera3.modules.maps.ui.pin.model.PinView

class EventSmallPinView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attrs, defStyle), PinView {

    private val binding: ViewEventPinSmallBinding = LayoutInflater.from(context)
        .inflate(R.layout.view_event_pin_small, this, false)
        .apply(::addView)
        .let(ViewEventPinSmallBinding::bind)

    override fun getAnchorX(): Float = EVENT_MARKER_SMALL_ANCHOR_X

    override fun getAnchorY(): Float = EVENT_MARKER_SMALL_ANCHOR_Y

    fun show(uiModel: EventSmallPinUiModel) {
        binding.ivEventPinSmallEvent.setImageResource(uiModel.eventIconResId)
    }

    companion object {
        private const val EVENT_MARKER_SMALL_ANCHOR_X = 0.5f
        private const val EVENT_MARKER_SMALL_ANCHOR_Y = 0.96875f
    }
}
