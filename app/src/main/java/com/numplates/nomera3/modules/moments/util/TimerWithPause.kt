package com.numplates.nomera3.modules.moments.util

import android.os.CountDownTimer
import timber.log.Timber

private const val TAG = "TimerWithPause"

abstract class TimerWithPause(
    private val totalTimerLengthMs: Long,
    private val intervalMs: Long
) {

    var isPaused: Boolean = false
    var isCanceled: Boolean = false
    var isStarted: Boolean = false

    val isPlaying: Boolean
        get() = isStarted && !isCanceled && !isPaused

    var currentCountDownTimer: CountDownWithPause? = null

    var timeRemaining: Long = totalTimerLengthMs

    fun start() {
        Timber.tag(TAG).d("timer start")
        resetFlags()
        isStarted = true
        timeRemaining = totalTimerLengthMs
        currentCountDownTimer = CountDownWithPause(totalTimerLengthMs, intervalMs)
        currentCountDownTimer?.start()
    }

    fun resume() {
        Timber.tag(TAG).d("timer resume")
        if (isPaused && isStarted) {
            isPaused = false
            currentCountDownTimer = CountDownWithPause(timeRemaining, intervalMs)
            currentCountDownTimer?.start()
        } else if (!isStarted) {
            start()
        }
    }

    fun pause() {
        Timber.tag(TAG).d("timer pause")
        isPaused = true
        currentCountDownTimer?.cancel()
    }

    fun cancel() {
        Timber.tag(TAG).d("timer cancel")
        resetFlags()
        isCanceled = true
        currentCountDownTimer?.cancel()
        timeRemaining = totalTimerLengthMs
    }

    abstract fun onTotalProgress(progress: Float)

    abstract fun onFinishCompletely()

    private fun resetFlags() {
        isStarted = false
        isPaused = false
        isCanceled = false
    }

    private fun onTick(millisUntilFinished: Long) {
        onTotalProgress((totalTimerLengthMs - millisUntilFinished) / totalTimerLengthMs.toFloat())
    }

    private fun onFinished() {
        isStarted = false
        onFinishCompletely()
    }

    inner class CountDownWithPause(
        timerLengthMs: Long,
        intervalMs: Long
    ) : CountDownTimer(timerLengthMs, intervalMs) {

        override fun onTick(millisUntilFinished: Long) {
            if (isPaused || isCanceled) {
                cancel()
            } else {
                timeRemaining = millisUntilFinished
                this@TimerWithPause.onTick(millisUntilFinished)
            }
        }

        override fun onFinish() = onFinished()
    }

}
