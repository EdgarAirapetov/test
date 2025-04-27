package com.numplates.nomera3.modules.auth.ui

sealed class AuthNavigationEvent {
    data object StartAuthProcess : AuthNavigationEvent()
    data class RollOutToScreenBeforeAuth(val isFirstLogin: Boolean) : AuthNavigationEvent()
    data object Complete : AuthNavigationEvent()
    data object BackNavigatePhoneScreenByCloseButton : AuthNavigationEvent()
    data object BackNavigatePhoneScreenByBack : AuthNavigationEvent()
}
