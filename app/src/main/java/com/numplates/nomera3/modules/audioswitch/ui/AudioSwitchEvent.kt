package com.numplates.nomera3.modules.audioswitch.ui

import com.twilio.audioswitch.AudioDevice

sealed class AudioSwitchEvent {

    /**
     * Select device
     */
    data class SelectDevice(val device: AudioDevice) : AudioSwitchEvent()

    /**
     * Finish audio switching flow
     */
    object FinishFlow : AudioSwitchEvent()
}
