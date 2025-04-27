package com.numplates.nomera3.modules.bump.hardware

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import javax.inject.Inject

class ShakeVibrator @Inject constructor(
    private val context: Context
) {
    private val vibrator: Vibrator by lazy {
        createVibration()
    }
    private val shakeVibrationWave = longArrayOf(300, 250, 150, 100)
    private val shakeVibrationSingleTime = 100L

    fun vibrate() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(shakeVibrationWave, REPEAT_VIBRATION_NONE))
        } else {
            vibrator.vibrate(DEFAULT_VIBRATE)
        }
    }

    fun vibrateSingleTime() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(shakeVibrationSingleTime, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            vibrator.vibrate(shakeVibrationSingleTime)
        }
    }

    fun hasVibrator() = vibrator.hasVibrator()

    fun cancel() {
        vibrator.cancel()
    }

    private fun createVibration(): Vibrator {
        val vibration = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            manager?.defaultVibrator
        } else {
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
        if (vibration == null) {
            throw VibrationNotFoundException("Vibrator for shake must not be null!")
        }
        return vibration
    }

    companion object {
        private const val DEFAULT_VIBRATE = 300L
        private const val REPEAT_VIBRATION_NONE = -1
    }
}
