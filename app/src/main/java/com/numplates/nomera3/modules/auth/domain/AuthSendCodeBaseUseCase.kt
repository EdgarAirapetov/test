package com.numplates.nomera3.modules.auth.domain

import com.numplates.nomera3.modules.auth.data.AuthenticationErrors
import com.numplates.nomera3.modules.auth.data.SendCodeErrors
import com.numplates.nomera3.modules.baseCore.DefParams

interface AuthSendCodeBaseUseCase<P : DefParams, T> {
    suspend fun execute(params: P, success: (T, Long?, Long?) -> Unit, fail: (SendCodeErrors) -> Unit)
}

interface AuthAuthenticateBaseUseCase<P : DefParams, T> {
    suspend fun execute(params: P, success: (T) -> Unit, fail: (AuthenticationErrors) -> Unit)
}

interface BaseAuthenticateUseCaseNoSuspend<P : DefParams, T> {

    fun execute(params: P): T
}
