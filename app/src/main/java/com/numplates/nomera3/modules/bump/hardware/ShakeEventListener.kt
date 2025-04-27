package com.numplates.nomera3.modules.bump.hardware

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.SystemClock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val SENSITIVITY_HARD = 15

// the magnitude of total acceleration
private const val TIME_THRESHOLD: Long = 1500

private const val DATA_X_INDEX = 0
private const val DATA_Y_INDEX = 1
private const val DATA_Z_INDEX = 2

/**
 * Detects phone shaking. If more than 75% of the samples taken in the past 0.5s are
 * accelerating, the device is a) shaking, or b) free falling 1.84m (h =
 * 1/2*g*t^2*3/4).
 */
interface ShakeEventListener {
    fun registerShakeEventListener(): Boolean
    fun unregisterShakeEventListener()
    fun observeShakeChanged(): SharedFlow<Unit>
    fun isSensorRunning(): Boolean
}

class ShakeEventListenerImpl @Inject constructor(
    private val appContext: Context,
    private val shakeVibrator: ShakeVibrator
) : ShakeEventListener, SensorEventListener {

    private var sensorEventManager: SensorManager? = null
    private var isSensorManagerRunning = false
    private var sensorAccelerometer: Sensor? = null
    private var queue: SampleQueue? = null
    private var mLastTime: Long = 0

    /**
     * When the magnitude of total acceleration exceeds this
     * value, the phone is accelerating.
     */
    private var defaultAccelerationThreshold = SENSITIVITY_HARD

    private val bumpScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val _bumpEvent = MutableSharedFlow<Unit>()

    override fun registerShakeEventListener(): Boolean {
        isSensorManagerRunning = true
        sensorEventManager = appContext.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        sensorAccelerometer = sensorEventManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        queue = SampleQueue()
        return sensorEventManager?.registerListener(
            this, sensorAccelerometer, SensorManager.SENSOR_DELAY_FASTEST
        ) ?: false
    }

    override fun unregisterShakeEventListener() {
        sensorEventManager?.unregisterListener(this)
        sensorEventManager = null
        sensorAccelerometer = null
        queue = null
        isSensorManagerRunning = false
        bumpScope.coroutineContext.cancelChildren()
        shakeVibrator.cancel()
    }

    override fun observeShakeChanged(): SharedFlow<Unit> = _bumpEvent.asSharedFlow()

    override fun isSensorRunning(): Boolean = isSensorManagerRunning

    override fun onSensorChanged(event: SensorEvent?) {
        when (event?.sensor?.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                handleAccelerometerEvent(event)
            }
            else -> Unit
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

    private fun handleAccelerometerEvent(event: SensorEvent?) {
        val accelerating = isAccelerating(event)
        val timestamp = event?.timestamp ?: 0
        queue?.add(timestamp, accelerating)
        val now = SystemClock.elapsedRealtime()
        val isShaking = queue?.isShaking ?: false
        if (isShaking && isTimeOut(now)) {
            queue?.clear()
            shakeVibrator.vibrate()
            emitShakeEvent()
            mLastTime = SystemClock.elapsedRealtime()
        }
    }

    private fun emitShakeEvent(){
        bumpScope.launch {
            _bumpEvent.emit(Unit)
        }
    }

    private fun isTimeOut(now: Long): Boolean {
        return now - mLastTime > TIME_THRESHOLD
    }

    /**
     * Returns true if the device is currently accelerating.
     * Instead of comparing magnitude to ACCELERATION_THRESHOLD,
     * compare their squares. This is equivalent and doesn't need the
     * actual magnitude, which would be computed using (expensive) Math.sqrt().
     */
    private fun isAccelerating(event: SensorEvent?): Boolean {
        val ax = event?.values?.get(DATA_X_INDEX) ?: return false
        val ay = event.values[DATA_Y_INDEX]
        val az = event.values[DATA_Z_INDEX]
        val magnitudeSquared = (ax * ax + ay * ay + az * az).toDouble()
        return magnitudeSquared > defaultAccelerationThreshold * defaultAccelerationThreshold
    }

    /**
     * Queue of samples. Keeps a running average.
     * https://github.com/square/seismic/blob/master/library/src/main/java/com/squareup/seismic/ShakeDetector.java
     */
    internal class SampleQueue {

        /**
         * Returns true if we have enough samples and more than 3/4 of those samples
         * are accelerating.
         */
        val isShaking: Boolean
            get() = (newest != null) && (oldest != null)
                && (getNewestTimeStamp() - getOldestTimeStamp() >= MIN_WINDOW_SIZE)
                && (acceleratingCount >= (sampleCount shr 1) + (sampleCount shr 2))

        private val pool: SamplePool = SamplePool()
        private var oldest: Sample? = null
        private var newest: Sample? = null
        private var sampleCount: Int = 0
        private var acceleratingCount: Int = 0

        /**
         * Adds a sample.
         *
         * @param timestamp    in nanoseconds of sample
         * @param accelerating true if > [.accelerationThreshold].
         */
        fun add(timestamp: Long, accelerating: Boolean) {
            purge(timestamp - MAX_WINDOW_SIZE)
            val added: Sample = pool.acquire()
            added.timestamp = timestamp
            added.accelerating = accelerating
            added.next = null
            if (newest != null) {
                newest?.next = added
            }
            newest = added
            if (oldest == null) {
                oldest = added
            }
            sampleCount++
            if (accelerating) {
                acceleratingCount++
            }
        }

        /**
         * Removes all samples from this queue.
         */
        fun clear() {
            while (oldest != null) {
                val removed: Sample = oldest ?: return
                oldest = removed.next
                pool.release(removed)
            }
            newest = null
            sampleCount = 0
            acceleratingCount = 0
        }

        /**
         * Purges samples with timestamps older than cutoff.
         */
        fun purge(cutoff: Long) {
            while ((sampleCount >= MIN_QUEUE_SIZE) && (oldest != null) && (cutoff - getOldestTimeStamp() > 0)) {
                val removed: Sample = oldest ?: return
                if (removed.accelerating) {
                    acceleratingCount--
                }
                sampleCount--
                oldest = removed.next
                if (oldest == null) {
                    newest = null
                }
                pool.release(removed)
            }
        }

        /**
         * Copies the samples into a list, with the oldest entry at index 0.
         */
        fun asList(): List<Sample> {
            val list: MutableList<Sample> = ArrayList()
            var s: Sample? = oldest
            while (s != null) {
                list.add(s)
                s = s.next
            }
            return list
        }

        private fun getOldestTimeStamp(): Long {
            return oldest?.timestamp ?: -1
        }

        private fun getNewestTimeStamp(): Long {
            return newest?.timestamp ?: -1
        }

        companion object {
            /**
             * Window size in ns. Used to compute the average.
             */
            private const val MAX_WINDOW_SIZE: Long = 500000000 // 0.5s
            private const val MIN_WINDOW_SIZE: Long = MAX_WINDOW_SIZE shr 1 // 0.25s

            /**
             * Ensure the queue size never falls below this size, even if the device
             * fails to deliver this many events during the time window. The LG Ally
             * is one such device.
             */
            private const val MIN_QUEUE_SIZE: Int = 4
        }
    }

    /**
     * An accelerometer sample.
     */
    internal class Sample {
        /**
         * Time sample was taken.
         */
        var timestamp: Long = 0

        /**
         * If acceleration > [.accelerationThreshold].
         */
        var accelerating: Boolean = false

        /**
         * Next sample in the queue or pool.
         */
        var next: Sample? = null
    }

    /**
     * Pools samples. Avoids garbage collection.
     */
    internal class SamplePool {
        private var head: Sample? = null

        /**
         * Acquires a sample from the pool.
         */
        fun acquire(): Sample {
            var acquired: Sample? = head
            if (acquired == null) {
                acquired = Sample()
            } else {
                head = acquired.next
            }
            return acquired
        }

        /**
         * Returns a sample to the pool.
         */
        fun release(sample: Sample) {
            sample.next = head
            head = sample
        }
    }
}

