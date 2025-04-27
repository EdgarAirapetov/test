package com.numplates.nomera3.modules.auth.domain

import com.numplates.nomera3.modules.auth.data.SendCodeErrors
import com.numplates.nomera3.modules.auth.data.repository.AuthRepositoryImpl
import com.numplates.nomera3.modules.baseCore.DefParams
import javax.inject.Inject

class AuthSendCodeSendCodeEmailUseCase @Inject constructor(
    private val repository: AuthRepositoryImpl
) : AuthSendCodeBaseUseCase<AuthSendCodeEmailParams, Boolean> {

    override suspend fun execute(
        params: AuthSendCodeEmailParams,
        success: (Boolean, Long?, Long?) -> Unit,
        fail: (SendCodeErrors) -> Unit
    ) {
        repository.sendCodeEmail(params.email, success, fail)
    }

}

class AuthSendCodeEmailParams(
    val email: String
) : DefParams()
