package com.numplates.nomera3.presentation.viewmodel.viewevents

sealed class DeeplinkActionViewEvent {
    class HandleDeepLink(val deeplink: String) : DeeplinkActionViewEvent()
    object ParseError : DeeplinkActionViewEvent()
    object NotAuthorized : DeeplinkActionViewEvent()
}