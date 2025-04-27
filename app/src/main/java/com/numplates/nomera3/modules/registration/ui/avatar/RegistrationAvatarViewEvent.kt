package com.numplates.nomera3.modules.registration.ui.avatar

import com.numplates.nomera3.presentation.viewmodel.userpersonalinfo.UniqueUsernameValidationResult

sealed class RegistrationAvatarViewEvent {
    data class ShowAvatar(val avatarState: String): RegistrationAvatarViewEvent()
    data class ShowPhoto(val path: String): RegistrationAvatarViewEvent()
    object UniqueNameValid: RegistrationAvatarViewEvent()
    object UniqueNameNotValid: RegistrationAvatarViewEvent()
    object UniqueNameNotAllowed: RegistrationAvatarViewEvent()
    object UniqueNameAlreadyTaken: RegistrationAvatarViewEvent()
    data class SetUniqueName(val uniqueName: String?): RegistrationAvatarViewEvent()
    data class UniqueNameValidated(val result: UniqueUsernameValidationResult): RegistrationAvatarViewEvent()
    object None: RegistrationAvatarViewEvent()
    object FinishRegistration: RegistrationAvatarViewEvent()
}
