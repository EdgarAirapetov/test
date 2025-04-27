package com.numplates.nomera3.modules.registration.ui.code

sealed class RegistrationCodeViewState {
    sealed class Error: RegistrationCodeViewState() {
        object IncorrectCode: Error()
        object NetworkError: Error()
    }
    object ClearInputTextViewState: RegistrationCodeViewState()
    data class AuthenticationFailed(val message: String? = null): RegistrationCodeViewState()
}
