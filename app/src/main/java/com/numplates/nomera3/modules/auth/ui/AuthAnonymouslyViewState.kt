package com.numplates.nomera3.modules.auth.ui

sealed class AuthAnonymouslyViewState {
    object AuthInProgress: AuthAnonymouslyViewState()
    object AuthSuccess: AuthAnonymouslyViewState()
    object AuthError: AuthAnonymouslyViewState()
}
