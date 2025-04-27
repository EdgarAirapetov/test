package com.numplates.nomera3.modules.auth.data

import com.numplates.nomera3.modules.auth.AuthStatus

class AuthStatusMapper {

    fun mapToPref(status: AuthStatus): Int {
        return when (status) {
            is AuthStatus.Unspecified -> 0
            is AuthStatus.None -> 1
            is AuthStatus.Anon -> return when (status.reason) {
                AuthStatus.Anon.Reason.ConnectedAfterNone -> 2
                AuthStatus.Anon.Reason.DeclinedAfterLogin -> 3
            }
            is AuthStatus.Authorized -> return when (status.reason) {
                AuthStatus.Authorized.Reason.ConnectedOnAppStart -> 4
                AuthStatus.Authorized.Reason.ConnectedAfterLoginOrRegistration -> 5
            }
        }
    }

    fun mapFromPref(prefStatus: Int): AuthStatus {
        return when (prefStatus) {
            0 -> AuthStatus.Unspecified
            1 -> AuthStatus.None
            2 -> AuthStatus.Anon(AuthStatus.Anon.Reason.ConnectedAfterNone)
            3 -> AuthStatus.Anon(AuthStatus.Anon.Reason.DeclinedAfterLogin)
            4 -> AuthStatus.Authorized(AuthStatus.Authorized.Reason.ConnectedOnAppStart)
            5 -> AuthStatus.Authorized(AuthStatus.Authorized.Reason.ConnectedAfterLoginOrRegistration)
            else -> AuthStatus.None
        }
    }

}