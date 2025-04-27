package com.numplates.nomera3.modules.registration.ui.location

sealed class RegistrationCityViewEvent {
    object ShowCitiesDialogEvent : RegistrationCityViewEvent()
    object ShowCountriesDialogEvent : RegistrationCityViewEvent()
    object GoToNextStep: RegistrationCityViewEvent()
}
