package com.numplates.nomera3.modules.registration.ui.birthday

sealed class RegistrationBirthdayViewEvent {
    data class BirthdayData(val birthday: String?, val hideAge: Boolean): RegistrationBirthdayViewEvent()
    object GoToNextStep: RegistrationBirthdayViewEvent()
    object None: RegistrationBirthdayViewEvent()
}