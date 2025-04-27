package com.numplates.nomera3.presentation.viewmodel.viewevents

sealed class UserProfileViewEvent {
    object ProfileDeleteSuccess: UserProfileViewEvent()
    object ProfileDeleteError: UserProfileViewEvent()

    object ProfileRecoverySuccess: UserProfileViewEvent()
    object ProfileRecoveryError: UserProfileViewEvent()
}