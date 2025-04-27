package com.numplates.nomera3.modules.userprofile.ui.fragment

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.view.View

class UserInfoSensorEventListener(
    private val leftViews: List<View>, private val rightViews: List<View>
) : SensorEventListener {

    private var gravity: Float = 0f

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            gravity = SCALE_DELTA * gravity + (1 - SCALE_DELTA) * event.values[0]
            val limitedX = limitValue(gravity)

            val rightScale = BASE_SCALE + (limitedX / SCALE_LIMIT) * SCALE_RANGE
            val leftScale = OPPOSITE_SCALE - rightScale

            leftViews.forEach {
                it.scaleX = leftScale
                it.scaleY = leftScale
            }

            rightViews.forEach {
                it.scaleX = rightScale
                it.scaleY = rightScale
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

    private fun limitValue(value: Float): Float {
        return when {
            value < -SCALE_LIMIT -> -SCALE_LIMIT
            value > SCALE_LIMIT -> SCALE_LIMIT
            else -> value
        }
    }

    companion object {
        private const val SCALE_LIMIT = 5f    // Ограничение наклона
        private const val SCALE_DELTA = 0.99f // Сглаживание
        private const val SCALE_RANGE = 0.2f  // Разброс масштабирования (0.8 - 1.2)
        private const val BASE_SCALE = 1.0f      // Базовый масштаб
        private const val OPPOSITE_SCALE = 2.0f  // Обратный масштаб
    }
}
