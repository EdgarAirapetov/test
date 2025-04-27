package com.numplates.nomera3.modules.auth.data

import androidx.annotation.StringRes

sealed class AuthenticationErrors {

    class NetworkAuthenticationError(@StringRes val messageRes: Int): AuthenticationErrors()

    class AuthenticateError(@StringRes val messageRes: Int) : AuthenticationErrors()

    class AuthenticateErrorExt(
        @StringRes val messageRes: Int,
        val field: String,
        val reason: String
    ) : AuthenticationErrors()

}
