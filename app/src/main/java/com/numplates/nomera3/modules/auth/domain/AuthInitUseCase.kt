package com.numplates.nomera3.modules.auth.domain

import com.numplates.nomera3.modules.auth.data.repository.AuthRepositoryImpl
import javax.inject.Inject

class AuthInitUseCase @Inject constructor(
    private val repository: AuthRepositoryImpl
) {
    suspend fun init() {
        repository.init()
    }
}