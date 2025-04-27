package com.numplates.nomera3.modules.auth.domain

import com.numplates.nomera3.modules.auth.data.AuthenticationErrors
import com.numplates.nomera3.modules.auth.data.repository.AuthRepositoryImpl
import com.numplates.nomera3.modules.baseCore.DefParams
import javax.inject.Inject

class AuthAuthenticatePhoneUseCase @Inject constructor(
    private val repository: AuthRepositoryImpl
) : AuthAuthenticateBaseUseCase<AuthAuthenticatePhoneParams, Boolean> {

    override suspend fun execute(
        params: AuthAuthenticatePhoneParams,
        success: (Boolean) -> Unit,
        fail: (AuthenticationErrors) -> Unit
    ) {
        repository.authenticatePhone(params.phone, params.code, success, fail)
    }
}

class AuthAuthenticatePhoneParams(
    val phone: String,
    val code: String
) : DefParams()