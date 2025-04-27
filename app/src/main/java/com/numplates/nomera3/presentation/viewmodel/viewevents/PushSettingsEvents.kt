package com.numplates.nomera3.presentation.viewmodel.viewevents

sealed class PushSettingsEvents {
    object ErrorGetSettings: PushSettingsEvents()
    object ErrorSetSettings: PushSettingsEvents()
}