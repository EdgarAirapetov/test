package com.numplates.nomera3.modules.auth.domain

import com.numplates.nomera3.modules.auth.data.SendCodeErrors
import com.numplates.nomera3.modules.auth.data.repository.AuthRepositoryImpl
import com.numplates.nomera3.modules.baseCore.DefParams
import javax.inject.Inject

class AuthSendCodeSendCodePhoneUseCase @Inject constructor(
    private val repository: AuthRepositoryImpl
) : AuthSendCodeBaseUseCase<AuthSendCodePhoneParams, Boolean> {

    override suspend fun execute(
        params: AuthSendCodePhoneParams,
        success: (Boolean, Long?, Long?) -> Unit,
        fail: (SendCodeErrors) -> Unit
    ) {
        repository.sendCodePhone(params.phone, success, fail)
    }

}

class AuthSendCodePhoneParams(
    val phone: String
) : DefParams()
