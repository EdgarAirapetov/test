package com.numplates.nomera3.modules.auth.domain

import com.numplates.nomera3.modules.auth.data.repository.AuthRepositoryImpl
import javax.inject.Inject

class AuthLogoutUseCase @Inject constructor(
    private val repository: AuthRepositoryImpl
) {

    suspend fun logout() {
        repository.logout()
    }

}
