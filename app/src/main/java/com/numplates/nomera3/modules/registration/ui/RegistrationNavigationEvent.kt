package com.numplates.nomera3.modules.registration.ui

import com.numplates.nomera3.modules.registration.AuthType

sealed class RegistrationNavigationEvent {
    data class ShowAuthCode(
        val authType: AuthType,
        val sendTo: String,
        val countryCode: String?,
        val countryName: String?,
        val countryMask: String?,
        val smsTimeout: Long?
    ) : RegistrationNavigationEvent()

    object ShowRegistrationName : RegistrationNavigationEvent()
    data class ShowRegistrationBirthday(val countryName: String?) : RegistrationNavigationEvent()
    data class ShowRegistrationDeleteProfileFragment(val countryName: String?) : RegistrationNavigationEvent()
    data object ShowRegistrationRottenProfileFragment : RegistrationNavigationEvent()
    data object ShowRegistrationPhoneFragment : RegistrationNavigationEvent()
    data class ShowRegistrationGender(val countryName: String?) : RegistrationNavigationEvent()
    data class ShowRegistrationLocation(val countryName: String?) : RegistrationNavigationEvent()
    data class ShowRegistrationAvatar(val countryName: String?) : RegistrationNavigationEvent()
    object RegistrationEmail : RegistrationNavigationEvent()
    object FinishRegistration : RegistrationNavigationEvent()
    data class CreateAvatar(val avatarState: String) : RegistrationNavigationEvent()
    object GoBack : RegistrationNavigationEvent()
    object None : RegistrationNavigationEvent()
}
