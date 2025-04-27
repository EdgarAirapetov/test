package com.numplates.nomera3.modules.auth.domain

import com.numplates.nomera3.modules.auth.data.AuthenticationErrors
import com.numplates.nomera3.modules.auth.data.repository.AuthRepositoryImpl
import javax.inject.Inject

class AuthAuthenticateAnonymouslyUseCase @Inject constructor(
    private val repository: AuthRepositoryImpl
) {

    suspend fun execute(
        success: (Boolean) -> Unit,
        fail: (AuthenticationErrors) -> Unit
    ) {
        repository.authenticateAnonymously(success, fail)
    }
}