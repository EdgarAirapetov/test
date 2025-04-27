package com.meera.core.utils.vibration

import android.Manifest.permission.VIBRATE
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.annotation.RequiresPermission

private const val DEFAULT_VIBRATE = 300L
private const val REPEAT_VIBRATION_NONE = -1
private val shakeVibrationWave = longArrayOf(300, 250, 150, 100)

fun Context.createVibration(): Vibrator? {
    val vibration = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val manager = this.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        manager?.defaultVibrator
    } else {
        this.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }
    return vibration
}

@RequiresPermission(anyOf = [VIBRATE])
fun Vibrator.vibrate() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        this.vibrate(VibrationEffect.createWaveform(shakeVibrationWave, REPEAT_VIBRATION_NONE))
    } else {
        this.vibrate(DEFAULT_VIBRATE)
    }
}
