package com.numplates.nomera3.modules.registration.ui.code

import android.os.CountDownTimer
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class TimerViewModel : ViewModel() {

    val liveData = MutableLiveData<TimerViewEvent>()

    private var timer: CountDownTimer? = null

    fun clearTimer() {
        timer?.cancel()
        timer = null
    }

    fun startTimer(smsResendCodeTimeout: Long?) {
        clearTimer()
        val millisInFuture = smsResendCodeTimeout ?: SMS_RESEND_CODE_TIMEOUT_DEFAULT
        timer = object : CountDownTimer(millisInFuture, COUNTER_UPDATE_INTERVAL) {

            override fun onTick(millisUntilFinished: Long) {
                liveData.value = TimerViewEvent.Time((millisUntilFinished / COUNTER_UPDATE_INTERVAL) + 1)
            }

            override fun onFinish() {
                liveData.value = TimerViewEvent.TimerFinished
            }
        }
        timer?.start()
    }

    override fun onCleared() {
        super.onCleared()
        clearTimer()
    }

    companion object {
        private const val SMS_RESEND_CODE_TIMEOUT_DEFAULT = 1000L * 60L
        private const val COUNTER_UPDATE_INTERVAL: Long = 1000
    }
}
