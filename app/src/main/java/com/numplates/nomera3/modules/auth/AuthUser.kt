package com.numplates.nomera3.modules.auth

data class AuthUser(
    val userId: Long = 0,

    val authStatus: AuthStatus = AuthStatus.None,

    val authToken: String = "",

    val refreshToken: String = "",

    val expiresToken: Long? = 0,

    val isProfileFilled: Boolean = false,

    val isProfileDeleted: Boolean = false,

    val isRegistrationCompleted: Boolean = false,
) {
    fun isProfileReadyToAuth(): Boolean {
        return isProfileFilled && isRegistrationCompleted && !isProfileDeleted
    }

    fun getReadyAuthStatus(): AuthStatus {
        return if (isProfileReadyToAuth()) {
            authStatus
        } else {
            AuthStatus.None
        }
    }
}

sealed class AuthStatus {

    object Unspecified : AuthStatus()

    object None : AuthStatus()

    data class Anon(val reason: Reason) : AuthStatus() {
        enum class Reason {
            ConnectedAfterNone,
            DeclinedAfterLogin
        }
    }

    data class Authorized(val reason: Reason) : AuthStatus() {
        enum class Reason {
            ConnectedOnAppStart,
            ConnectedAfterLoginOrRegistration

            // TODO: 28.06.2021 Делать ли событие без uId ?
        }
    }
}

