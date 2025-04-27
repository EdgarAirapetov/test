package com.numplates.nomera3.modules.maps.ui.events

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.meera.uikit.widgets.chips.ImageChipBackground
import com.meera.uikit.widgets.chips.UiKitChipView
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.ViewEventChipsBinding
import com.numplates.nomera3.modules.maps.ui.events.model.EventChipsType
import com.numplates.nomera3.modules.maps.ui.events.model.EventChipsUiModel

class EventChipsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
): LinearLayout(context, attrs, defStyle) {

    private val binding = LayoutInflater.from(context)
        .inflate(R.layout.view_event_chips, this)
        .let(ViewEventChipsBinding::bind)

    init {
        orientation = HORIZONTAL
    }

    fun setModel(uiModel: EventChipsUiModel) {
        binding.ukicvEventType.setImageResId(uiModel.label.imgResId)
        binding.ukicvEventType.getConfig()?.let { config ->
            val background =  when (uiModel.type) {
                EventChipsType.LIGHT -> ImageChipBackground.LIGHT
                EventChipsType.DARK, EventChipsType.VIP -> ImageChipBackground.DARK
            }
            val updatedConfig = config.copy(
                text = context.getString(uiModel.label.titleResId),
                background = background
            )
            binding.ukicvEventType.setConfig(updatedConfig)
        }
        binding.ukcvDateTime.text = "${uiModel.label.date}, ${uiModel.label.time}"
        binding.ukcvDateTime.chipType = when (uiModel.type) {
            EventChipsType.LIGHT -> UiKitChipView.ChipType.LEGACY_LIGHT
            EventChipsType.DARK, EventChipsType.VIP -> UiKitChipView.ChipType.LEGACY_DARK
        }
        if (uiModel.type == EventChipsType.VIP) {
            val vipBgColorStateList = ColorStateList.valueOf(context.getColor(R.color.ui_black_vip_background))
            binding.ukicvEventType.backgroundTintList = vipBgColorStateList
            binding.ukcvDateTime.backgroundTintList = vipBgColorStateList
        }
    }
}
