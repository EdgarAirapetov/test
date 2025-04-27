package com.numplates.nomera3.modules.audioswitch.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.meera.core.extensions.click
import com.numplates.nomera3.App
import com.numplates.nomera3.databinding.BottomSheetSwitchAudioBinding
import com.numplates.nomera3.presentation.router.BaseBottomSheetDialogFragment
import com.twilio.audioswitch.AudioDevice
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class AudioSwitchBottomSheet : BaseBottomSheetDialogFragment<BottomSheetSwitchAudioBinding>() {

    interface Listener {
        fun onDeviceSelected(device: AudioDevice)
    }

    var availableDevices: List<AudioDevice>? = null
    var selectedDevice: AudioDevice? = null

    private val viewModel by viewModels<AudioSwitchViewModel> { App.component.getViewModelFactory() }
    private var listener: Listener? = null

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> BottomSheetSwitchAudioBinding
        get() = BottomSheetSwitchAudioBinding::inflate

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = parentFragment as Listener
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val audioAdapter = AudioSwitchAdapter { device -> viewModel.selectDevice(device) }
        binding?.cancel?.click { dismiss() }
        binding?.devices?.apply {
            itemAnimator = null
            adapter = audioAdapter
        }
        setupListeners()
        if (savedInstanceState == null) {
            viewModel.fillItems(
                selected = selectedDevice,
                available = availableDevices ?: error("Can not be null"),
            )
        }
    }

    override fun show(manager: FragmentManager, tag: String?) {
        require(availableDevices != null) { "Please provide list of items before calling popup" }
        require(selectedDevice != null) { "Please provide list of items before calling popup" }
        super.show(manager, tag)
    }

    private fun setupListeners() {
        viewModel.devices.observe(viewLifecycleOwner) { devices ->
            (binding?.devices?.adapter as? AudioSwitchAdapter)?.submitList(devices)
        }
        viewModel.eventsFlow
            .flowWithLifecycle(lifecycle)
            .onEach { event ->
                when (event) {
                    is AudioSwitchEvent.SelectDevice -> {
                        listener?.onDeviceSelected(event.device)
                    }
                    is AudioSwitchEvent.FinishFlow -> {
                        dismiss()
                    }
                }
            }
            .launchIn(lifecycleScope)
    }

    companion object {
        fun showBottomMenu(
            fm: FragmentManager,
            devices: List<AudioDevice>,
            selected: AudioDevice?,
        ) {
            val bottomSheet = AudioSwitchBottomSheet().apply {
                availableDevices = devices
                selectedDevice = selected
            }
            bottomSheet.show(fm, AudioSwitchBottomSheet::javaClass.name)
        }

        fun isShowed(fm: FragmentManager): Boolean {
            return fm.findFragmentByTag(AudioSwitchBottomSheet::javaClass.name) != null
        }

        fun dismiss(fm: FragmentManager) {
            (fm.findFragmentByTag(AudioSwitchBottomSheet::javaClass.name) as? AudioSwitchBottomSheet)?.dismiss()
        }
    }
}
