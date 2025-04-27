package com.numplates.nomera3.modules.auth.ui

import com.numplates.nomera3.modules.registration.AuthType

sealed class AuthNavigationState {
    object None : AuthNavigationState()
    object Phone : AuthNavigationState()
    data class CodeConfirmation(
        val authType: AuthType,
        val sendTo: String,
        val countryCode: String?,
        val countryName: String?,
        val countryMask: String?,
        val smsTimeout: Long?
    ) : AuthNavigationState()
    data class Sms(val phoneNumber: String, val login: String) : AuthNavigationState()
    data class UserPersonalInfo(val countryName: String?) : AuthNavigationState()
}
