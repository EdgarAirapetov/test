package com.numplates.nomera3.presentation.viewmodel.viewevents

sealed class AddNumberViewEvent {
    class FailedToSendVerifyCode(
            val message: String
    ): AddNumberViewEvent()
    object VerifyCodeSend: AddNumberViewEvent()
    object VerifySuccess: AddNumberViewEvent()
    object VerifyFailure: AddNumberViewEvent()
    object TimerFinished: AddNumberViewEvent()
}