package com.numplates.nomera3.modules.auth.data.repository

import com.numplates.nomera3.modules.auth.AuthUser
import com.numplates.nomera3.modules.auth.data.AuthenticationErrors
import com.numplates.nomera3.modules.auth.data.SendCodeErrors
import io.reactivex.subjects.ReplaySubject

interface AuthRepository {

    suspend fun init()

    fun getAuthUserStateObservable(): ReplaySubject<AuthUser>

    suspend fun getAuthUser(): AuthUser

    fun isAuthorizedUser(): Boolean

    suspend fun sendCodeEmail(
        email: String,
        success: (Boolean, Long?, Long?) -> Unit,
        fail: (SendCodeErrors) -> Unit
    )

    suspend fun sendCodePhone(
        phone: String,
        success: (Boolean, Long?, Long?) -> Unit,
        fail: (SendCodeErrors) -> Unit
    )

    suspend fun authenticateEmail(
        email: String,
        code: String,
        success: (Boolean) -> Unit,
        fail: (AuthenticationErrors) -> Unit
    )

    suspend fun authenticatePhone(
        phone: String,
        code: String,
        success: (Boolean) -> Unit,
        fail: (AuthenticationErrors) -> Unit
    )

    suspend fun authenticateAnonymously(
        success: (Boolean) -> Unit,
        fail: (AuthenticationErrors) -> Unit
    )

    suspend fun logout()

}
