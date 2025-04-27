package com.numplates.nomera3.modules.registration.ui.code

sealed class RegistrationCodeViewEvent {
    data object AuthenticationSuccess: RegistrationCodeViewEvent()
    data class ShowErrorSnackEvent(val message: String? = null): RegistrationCodeViewEvent()
}
