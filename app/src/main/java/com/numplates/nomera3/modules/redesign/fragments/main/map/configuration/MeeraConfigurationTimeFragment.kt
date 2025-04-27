package com.numplates.nomera3.modules.redesign.fragments.main.map.configuration

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.meera.core.base.viewbinding.viewBinding
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.FragmentConfigurationTimeBinding
import com.numplates.nomera3.modules.maps.ui.events.model.TimePickerUiModel
import com.numplates.nomera3.modules.maps.ui.model.MapUiEffect
import com.numplates.nomera3.modules.redesign.fragments.base.MeeraBaseFragment
import timber.log.Timber
import java.time.LocalTime

private const val ARG_TIME_MODEL = "ARG_TIME_MODEL"

class MeeraConfigurationTimeFragment : MeeraBaseFragment(R.layout.fragment_configuration_time) {

    private var uiModel: TimePickerUiModel? = null

    private val binding by viewBinding(FragmentConfigurationTimeBinding::bind)

    private val viewModel: MeeraMapViewModel by viewModels(
        ownerProducer = { requireParentFragment() }
    )

//    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentConfigurationTimeBinding
//        get() = FragmentConfigurationTimeBinding::inflate


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        arguments?.let {
//            uiModel = it.getParcelable(ARG_TIME_MODEL)
//        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.uiEffectsFlow.collect(::handleUiEffect)
        }

        binding?.tpTimePicker?.setIs24HourView(true)
        binding?.tpTimePicker?.setOnTimeChangedListener { _, hourOfDay, minute ->
            val selectedTime = LocalTime.of(hourOfDay, minute)
//            val currentLocalTime = Calendar.getInstance().time
//            binding.tvTimePickerPopupSave.isEnabled =
//                uiModel.minimumTime == null || !selectedTime.isBefore(uiModel.minimumTime)
//            onTimeSelected(selectedTime)
            val result = viewModel.eventsOnMap.setSelectedTimeWithRes(selectedTime)
            binding?.tpTimePicker?.hour = result.hour
            binding?.tpTimePicker?.minute = result.minute
//            viewModel.eventsOnMap.onSelectTime()
        }
    }

    private fun handleUiEffect(uiEffect: MapUiEffect) {
        when (uiEffect) {
            is MapUiEffect.ShowEventTimePicker -> showEventTimePickerDialog(uiEffect.uiModel)
           else -> {}
        }
    }

    private fun showEventTimePickerDialog(uiModel: TimePickerUiModel) {
        Timber.e("showEventTimePickerDialog uiModel$uiModel")

//        binding?.ecwMapEventsConfiguration?.setTime(uiModel) { time ->
//            Timber.e("showEventTimePickerDialog called")
//            mapViewModel.eventsOnMap.setSelectedTime(time)
//        }

//        currentDialog?.dismiss()
//        currentDialog = TimePickerPopupDialog(
//            activity = requireActivity(),
//            uiModel = uiModel
//        ) { time ->
//            mapViewModel.eventsOnMap.setSelectedTime(time)
//        }.apply {
//            show()
//        }
    }

    fun setTimeConfig(uiModel: LocalTime, timeCallback: ((LocalTime) -> Unit)?) {
        Timber.e("$timeCallback")
//        this.uiModel = uiModel
//        binding?.tvTimezoneHint?.isGone = uiModel.isInUserTimezone
        binding?.tpTimePicker?.hour = uiModel.hour
        binding?.tpTimePicker?.minute = uiModel.minute
//        binding?.tpTimePicker?.setIs24HourView(true)
//        binding?.tpTimePicker?.setOnTimeChangedListener { _, hourOfDay, minute ->
//            val selectedTime = LocalTime.of(hourOfDay, minute)
////            binding.tvTimePickerPopupSave.isEnabled =
////                uiModel.minimumTime == null || !selectedTime.isBefore(uiModel.minimumTime)
////            onTimeSelected(selectedTime)
//            viewModel.eventsOnMap.onSelectTime()
//            timeCallback?.invoke(selectedTime)
//        }
    }

    companion object {

        @JvmStatic
        fun newInstance(uiModel: TimePickerUiModel) =
            MeeraConfigurationTimeFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_TIME_MODEL, uiModel)
                }
            }
    }
}
