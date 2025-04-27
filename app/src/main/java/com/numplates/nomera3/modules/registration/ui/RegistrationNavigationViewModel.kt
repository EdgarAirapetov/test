package com.numplates.nomera3.modules.registration.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.numplates.nomera3.modules.registration.AuthType

class RegistrationNavigationViewModel : ViewModel() {

    val eventLiveData = MutableLiveData<RegistrationNavigationEvent>()

    var isBackFromNameAvailable = true

    fun goBack() {
        event(RegistrationNavigationEvent.GoBack)
    }

    fun clearNavigationState() {
        event(RegistrationNavigationEvent.None)
    }

    fun authCodeIsSent(
        authType: AuthType,
        sendTo: String,
        countryCode: String?,
        countryName: String?,
        countryMask: String?,
        smsTimeout: Long?
    ) {
        event(
            RegistrationNavigationEvent.ShowAuthCode(
                authType,
                sendTo,
                countryCode,
                countryName,
                countryMask,
                smsTimeout
            )
        )
    }

    fun registrationDeleteProfileNext(countryName: String?) {
        event(RegistrationNavigationEvent.ShowRegistrationDeleteProfileFragment(countryName))
    }

    fun registrationRottenProfileNext() {
        event(RegistrationNavigationEvent.ShowRegistrationRottenProfileFragment)
    }

    fun registrationPhoneNext() {
        event(RegistrationNavigationEvent.ShowRegistrationPhoneFragment)
    }

    fun registrationNameNext(countryName: String?) {
        event(RegistrationNavigationEvent.ShowRegistrationBirthday(countryName))
    }

    fun registrationBirthdayNext(countryName: String?) {
        event(RegistrationNavigationEvent.ShowRegistrationGender(countryName))
    }

    fun registrationGenderNext(countryName: String?) {
        event(RegistrationNavigationEvent.ShowRegistrationLocation(countryName))
    }

    fun registrationLocationNext(countryName: String?) {
        event(RegistrationNavigationEvent.ShowRegistrationAvatar(countryName))
    }

    fun registrationEmailNext() {
        event(RegistrationNavigationEvent.RegistrationEmail)
    }

    fun finishRegistration() {
        event(RegistrationNavigationEvent.FinishRegistration)
    }

    fun registrationCreateAvatar(avatarState: String) {
        event(RegistrationNavigationEvent.CreateAvatar(avatarState))
    }

    private fun event(event: RegistrationNavigationEvent) {
        eventLiveData.postValue(event)
    }
}
