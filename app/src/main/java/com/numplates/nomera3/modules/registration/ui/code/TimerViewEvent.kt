package com.numplates.nomera3.modules.registration.ui.code

sealed class TimerViewEvent {
    data class Time(val time: Long): TimerViewEvent()
    object TimerFinished: TimerViewEvent()
}
