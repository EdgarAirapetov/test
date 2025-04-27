package com.numplates.nomera3.modules.audioswitch.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.numplates.nomera3.R
import com.twilio.audioswitch.AudioDevice
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

private const val DISMISS_DELAY = 300L

class AudioSwitchViewModel @Inject constructor() : ViewModel() {

    private val _devices = MutableLiveData<List<AudioSwitchUiModel>>()
    val devices = _devices as LiveData<List<AudioSwitchUiModel>>

    private val _eventsFlow = MutableSharedFlow<AudioSwitchEvent>()
    val eventsFlow = _eventsFlow.asSharedFlow()

    private var selectJob: Job? = null

    fun fillItems(selected: AudioDevice?, available: List<AudioDevice>) {
        val items = available.map { device ->
            Timber.d("selected device: $selected, device: $device, value: ${device.name == selected?.name},")
            AudioSwitchUiModel(
                device = device,
                iconRes = when (device) {
                    is AudioDevice.Speakerphone -> R.drawable.ic_switch_audio_speakerphone
                    is AudioDevice.Earpiece -> R.drawable.ic_switch_audio_earpiece
                    is AudioDevice.WiredHeadset -> R.drawable.ic_switch_audio_wired_headset
                    is AudioDevice.BluetoothHeadset -> R.drawable.ic_switch_audio_bluetooth_headset
                },
                titleRes = when (device) {
                    is AudioDevice.Speakerphone -> R.string.audio_switch_speakerphone
                    is AudioDevice.Earpiece -> R.string.audio_switch_earpiece
                    is AudioDevice.WiredHeadset -> R.string.audio_switch_wired_headset
                    is AudioDevice.BluetoothHeadset -> R.string.audio_switch_bluetooth_headset
                },
                isSelected = device.name == selected?.name,
            )
        }
        _devices.postValue(items)
    }

    fun selectDevice(device: AudioDevice) {
        selectJob?.cancel()
        selectJob = viewModelScope.launch {
            val updated = _devices.value?.map { item ->
                item.copy(isSelected = item.device == device)
            }
            _devices.postValue(updated)
            _eventsFlow.emit(AudioSwitchEvent.SelectDevice(device))
            delay(DISMISS_DELAY)
            _eventsFlow.emit(AudioSwitchEvent.FinishFlow)
        }
    }
}
