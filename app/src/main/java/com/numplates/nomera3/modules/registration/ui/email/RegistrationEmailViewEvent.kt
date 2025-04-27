package com.numplates.nomera3.modules.registration.ui.email

import com.numplates.nomera3.modules.registration.AuthType

sealed class RegistrationEmailViewEvent{

    sealed class Error: RegistrationEmailViewEvent() {
        object EmailEmpty: Error()
        object EmailIncorrect: Error()
        object SendCodeFailed: Error()
        data class UserBlocked(val reason: String?, val blockExpired: Long?): Error()
        data class UserNotFound(val reason: String?): Error()
    }
    data class SendCodeSuccess(
        val sendTo: String,
        val authType: AuthType,
        ): RegistrationEmailViewEvent()
    object ServerChanged: RegistrationEmailViewEvent()
    object None: RegistrationEmailViewEvent()
}
