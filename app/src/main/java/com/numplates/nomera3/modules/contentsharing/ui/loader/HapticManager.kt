package com.numplates.nomera3.modules.contentsharing.ui.loader

import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import com.meera.core.utils.vibration.createVibration
import javax.inject.Inject

private const val HAPTIC_DURATION = 100L

class HapticManager @Inject constructor(appContext: Context) {

    private val vibrator: Vibrator? = appContext.createVibration()

    fun pushHaptic() {
        vibrator?.vibrate(VibrationEffect.createOneShot(HAPTIC_DURATION, VibrationEffect.DEFAULT_AMPLITUDE))
    }
}
