package com.numplates.nomera3.modules.registration.ui.name

import androidx.annotation.StringRes

sealed class RegistrationNameViewEvent {
    data class Error(val message: String): RegistrationNameViewEvent()
    data class Name(val name: String?): RegistrationNameViewEvent()
    object NameAccepted: RegistrationNameViewEvent()
    object NameNotAccepted: RegistrationNameViewEvent()
    object GoToNextStep: RegistrationNameViewEvent()
    object None: RegistrationNameViewEvent()
    data class NameError(@StringRes val errorMessageRes: Int) : RegistrationNameViewEvent()
}
