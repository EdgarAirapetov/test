package com.numplates.nomera3.modules.audioswitch.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.meera.uikit.bottomsheetdialog.UiKitBottomSheetDialog
import com.meera.uikit.bottomsheetdialog.UiKitBottomSheetDialogBehDelegate
import com.meera.uikit.bottomsheetdialog.UiKitBottomSheetDialogParams
import com.meera.uikit.bottomsheetdialog.UiKitBottomSheetDialogState
import com.numplates.nomera3.App
import com.numplates.nomera3.R
import com.numplates.nomera3.databinding.MeeraAudioSwitchBottomSheetBinding
import com.twilio.audioswitch.AudioDevice
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

private const val MEERA_AUDIO_SWITCH_BOTTOM_SHEET_TAG = "MeeraAudioSwitchBottomSheet"


class MeeraAudioSwitchBottomSheet: UiKitBottomSheetDialog<MeeraAudioSwitchBottomSheetBinding>() {

    private val viewModel by viewModels<MeeraAudioSwitchViewModel> { App.component.getViewModelFactory() }

    private var availableDevices: List<AudioDevice>? = null
    private var selectedDevice: AudioDevice? = null
    private var onDeviceSelected: (device: AudioDevice) -> Unit = {}

    override val inflateContent: (LayoutInflater, ViewGroup?, Boolean) -> MeeraAudioSwitchBottomSheetBinding
        get() = MeeraAudioSwitchBottomSheetBinding::inflate

    override fun getBehaviorDelegate(): UiKitBottomSheetDialogBehDelegate {
        return UiKitBottomSheetDialogBehDelegate.Builder()
            .setBottomSheetState(UiKitBottomSheetDialogState.EXPANDED)
            .setDraggable(false)
            .setSkipCollapsed(true)
            .create(dialog)
    }

    override fun createDialogState(): UiKitBottomSheetDialogParams =
        UiKitBottomSheetDialogParams(labelText = context?.getString(R.string.general_actions))

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val audioAdapter = MeeraAudioSwitchAdapter { device -> viewModel.selectDevice(device) }
        contentBinding?.rvDevices?.apply {
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

    fun show(
        fm: FragmentManager,
        devices: List<AudioDevice>,
        selected: AudioDevice?,
        onDeviceSelected: (device: AudioDevice) -> Unit
    ): MeeraAudioSwitchBottomSheet {
        val dialog = MeeraAudioSwitchBottomSheet()
        dialog.availableDevices = devices
        dialog.selectedDevice = selected
        dialog.onDeviceSelected = onDeviceSelected
        dialog.isCancelable = this.isCancelable
        dialog.show(fm, MEERA_AUDIO_SWITCH_BOTTOM_SHEET_TAG)
        return dialog
    }

    private fun setupListeners() {
        viewModel.devices.observe(viewLifecycleOwner) { devices ->
            (contentBinding?.rvDevices?.adapter as? MeeraAudioSwitchAdapter)?.submitList(devices)
        }
        viewModel.eventsFlow
            .flowWithLifecycle(lifecycle)
            .onEach { event ->
                when (event) {
                    is AudioSwitchEvent.SelectDevice -> {
                        onDeviceSelected.invoke(event.device)
                    }
                    is AudioSwitchEvent.FinishFlow -> {
                        dismiss()
                    }
                }
            }
            .launchIn(lifecycleScope)
    }

    companion object {
        fun isShowed(fm: FragmentManager): Boolean {
            return fm.findFragmentByTag(MEERA_AUDIO_SWITCH_BOTTOM_SHEET_TAG) != null
        }
    }

}
