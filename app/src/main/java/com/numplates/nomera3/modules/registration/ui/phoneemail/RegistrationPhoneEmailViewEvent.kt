package com.numplates.nomera3.modules.registration.ui.phoneemail

import com.numplates.nomera3.modules.registration.AuthType
import com.numplates.nomera3.modules.registration.domain.model.RegistrationCountryModel

sealed class RegistrationPhoneEmailViewEvent{

    sealed class Error: RegistrationPhoneEmailViewEvent() {
        object PhoneEmpty: Error()
        object PhoneIncorrect: Error()
        object EmailEmpty: Error()
        object EmailIncorrect: Error()
        object SendCodeFailed: Error()
        data class UserBlocked(val reason: String?, val blockExpired: Long?): Error()
    }
    data class SendCodeSuccess(
        val sendTo: String,
        val authType: AuthType,
        val timeout: Long?,
        val blockTime: Long?,
        val countryCode: String?,
        val countryName: String?,
        val countryMask: String?
        ): RegistrationPhoneEmailViewEvent()
    object ServerChanged: RegistrationPhoneEmailViewEvent()
    data class None(val authType: AuthType, val phone: String?, val country: RegistrationCountryModel?, val showKeyboard:Boolean = true) :
        RegistrationPhoneEmailViewEvent()
    data class CountryDetected(val country: RegistrationCountryModel): RegistrationPhoneEmailViewEvent()
}
