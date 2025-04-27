package com.numplates.nomera3.modules.maps.ui.events

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.core.view.isGone
import androidx.fragment.app.FragmentActivity
import com.meera.core.extensions.setThrottledClickListener
import com.numplates.nomera3.databinding.DialogTimePickerPopupBinding
import com.numplates.nomera3.modules.maps.ui.events.model.TimePickerUiModel
import com.numplates.nomera3.modules.maps.ui.view.MapBottomSheetDialog
import java.time.LocalTime

class TimePickerPopupDialog(
    activity: FragmentActivity,
    private val uiModel: TimePickerUiModel,
    private val onTimeSelected: (LocalTime) -> Unit,
) : MapBottomSheetDialog(activity) {

    private val binding: DialogTimePickerPopupBinding = DialogTimePickerPopupBinding.inflate(layoutInflater)

    init {
        setContentView(binding.root)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.tvTimezoneHint.isGone = uiModel.isInUserTimezone
        binding.tpTimePicker.hour = uiModel.selectedTime.hour
        binding.tpTimePicker.minute = uiModel.selectedTime.minute
        binding.tpTimePicker.setIs24HourView(true)
        binding.tpTimePicker.setOnTimeChangedListener { _, hourOfDay, minute ->
            val selectedTime = LocalTime.of(hourOfDay, minute)
            binding.tvTimePickerPopupSave.isEnabled =
                uiModel.minimumTime == null || !selectedTime.isBefore(uiModel.minimumTime)
        }
        binding.ibTimePickerPopupClose.setThrottledClickListener {
            dismiss()
        }
        binding.tvTimePickerPopupSave.setThrottledClickListener {
            val selectedTime = LocalTime.of(binding.tpTimePicker.hour, binding.tpTimePicker.minute)
            onTimeSelected(selectedTime)
            dismiss()
        }
    }
}
