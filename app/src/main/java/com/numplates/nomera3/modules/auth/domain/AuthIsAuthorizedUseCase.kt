package com.numplates.nomera3.modules.auth.domain

import com.numplates.nomera3.modules.auth.data.repository.AuthRepositoryImpl
import javax.inject.Inject

class AuthIsAuthorizedUseCase @Inject constructor(
    private val repository: AuthRepositoryImpl
) {

    fun isAuthorizedUser() = repository.isAuthorizedUser()
}
