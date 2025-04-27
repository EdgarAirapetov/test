package com.numplates.nomera3.modules.chat.toolbar.ui.utils

import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import java.util.concurrent.atomic.AtomicBoolean

private const val TYPING_TIMEOUT_MS = 5000L
private const val DELAY_BEFORE_UPDATE_STATUS = 500L
private const val START_TYPING_POST_DELAY = 100L


class ChatTypingCountdownTimer(
    val onFinish: () -> Unit
) : CountDownTimer(TYPING_TIMEOUT_MS, TYPING_TIMEOUT_MS) {

    var isStarted = false

    private var isTypingRun = AtomicBoolean(false)
    private val typingHandler: Handler = Handler(Looper.getMainLooper())



    fun startTimer() {
        this.isStarted = true
        this.cancel()
        startTypingTimerAnimation()
        this.start()
    }

    override fun onFinish() {
        this.isStarted = false
        onFinish.invoke()
        isTypingRun.set(false)
        stopTypingTimerAnimation()
    }

    override fun onTick(p0: Long) {
        /** STUB */
    }

    private fun startTypingTimerAnimation() {
        if (!isTypingRun.get()) {
            typingHandler.postDelayed(typingRunnable, START_TYPING_POST_DELAY)
        }
        isTypingRun.set(true)
    }

    private fun stopTypingTimerAnimation() {
        typingHandler.removeCallbacks(typingRunnable)
    }

    private val typingRunnable = object : Runnable {
        override fun run() {
            try {
                // Timber.d("Update typing status")
            } finally {
                typingHandler.postDelayed(this, DELAY_BEFORE_UPDATE_STATUS)
            }
        }
    }

}
