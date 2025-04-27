package com.numplates.nomera3.modules.audioswitch.ui

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.twilio.audioswitch.AudioDevice

data class AudioSwitchUiModel(
    val device: AudioDevice,
    @DrawableRes
    val iconRes: Int,
    @StringRes
    val titleRes: Int,
    val isSelected: Boolean = false,
)
