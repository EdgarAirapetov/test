package com.numplates.nomera3.presentation.viewmodel.viewevents

sealed class PrivacyViewEvent{
    object FailedToLoadSettings: PrivacyViewEvent()
    object FailedToSetSettings: PrivacyViewEvent()
    object SettingsSavedEvent: PrivacyViewEvent()
}