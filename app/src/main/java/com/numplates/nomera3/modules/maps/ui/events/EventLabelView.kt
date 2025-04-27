package com.numplates.nomera3.modules.maps.ui.events

import android.content.Context
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.ViewEventLabelBinding
import com.numplates.nomera3.modules.maps.ui.events.model.EventLabelUiModel

class EventLabelView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
): ConstraintLayout(context, attrs, defStyle) {

    private val binding = LayoutInflater.from(context)
        .inflate(R.layout.view_event_label, this, false)
        .apply(::addView)
        .let(ViewEventLabelBinding::bind)

    fun setModel(eventLabelUiModel: EventLabelUiModel) {
        val textColor = resources.getColor(eventLabelUiModel.textColorResId, null)
        listOf(binding.tvEventLabelTitle, binding.tvEventLabelDate, binding.tvEventLabelDay, binding.tvEventLabelTime)
            .forEach { textView ->
                setTextViewColor(textView = textView, color = textColor)
                setTextViewTextSize(textView = textView, textSizeSp = eventLabelUiModel.textSizeSp)
            }
        setTextViewColor(textView = binding.tvEventLabelDistanceAddress, color = textColor)

        binding.ivEventLabelImage.setImageResource(eventLabelUiModel.imgResId)
        binding.tvEventLabelTitle.setText(eventLabelUiModel.titleResId)
        binding.tvEventLabelDate.text = eventLabelUiModel.date
        binding.tvEventLabelDay.text = eventLabelUiModel.day
        binding.tvEventLabelTime.text = eventLabelUiModel.time
        binding.tvEventLabelDistanceAddress.text = eventLabelUiModel.distanceAddress?.let {
            "${it.distanceString}, ${it.addressString}"
        }
        binding.tvEventLabelDistanceAddress.isVisible = eventLabelUiModel.distanceAddress != null
    }

    private fun setTextViewColor(textView: TextView, color: Int) {
        textView.setTextColor(color)
        textView.compoundDrawablesRelative.forEach { drawable ->
            drawable?.colorFilter = PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN)
        }
    }

    private fun setTextViewTextSize(textView: TextView, textSizeSp: Int) {
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSizeSp.toFloat())
    }
}
