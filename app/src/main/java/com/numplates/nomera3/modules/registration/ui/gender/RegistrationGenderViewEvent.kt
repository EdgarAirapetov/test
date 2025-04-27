package com.numplates.nomera3.modules.registration.ui.gender

sealed class RegistrationGenderViewEvent {
    data class Gender(val gender: Int?, val hideGender: Boolean, val hiddenAgeAndGender: Boolean) :
        RegistrationGenderViewEvent()

    data class SetContinueButtonAvailable(val isEnabled: Boolean) : RegistrationGenderViewEvent()
    object GoToNextStep : RegistrationGenderViewEvent()
    object None : RegistrationGenderViewEvent()
}
