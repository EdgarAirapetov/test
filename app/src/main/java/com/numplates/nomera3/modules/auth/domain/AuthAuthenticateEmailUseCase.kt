package com.numplates.nomera3.modules.auth.domain

import com.numplates.nomera3.modules.auth.data.AuthenticationErrors
import com.numplates.nomera3.modules.auth.data.repository.AuthRepositoryImpl
import com.numplates.nomera3.modules.baseCore.DefParams
import javax.inject.Inject

class AuthAuthenticateEmailUseCase @Inject constructor(
    private val repository: AuthRepositoryImpl
) : AuthAuthenticateBaseUseCase<AuthAuthenticateEmailParams, Boolean> {

    override suspend fun execute(
        params: AuthAuthenticateEmailParams,
        success: (Boolean) -> Unit,
        fail: (AuthenticationErrors) -> Unit
    ) {
        repository.authenticateEmail(params.email, params.code, success, fail)
    }

}

class AuthAuthenticateEmailParams(
    val email: String,
    val code: String
) : DefParams()
