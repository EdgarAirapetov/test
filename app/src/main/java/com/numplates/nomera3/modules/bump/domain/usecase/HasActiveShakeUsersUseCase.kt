package com.numplates.nomera3.modules.bump.domain.usecase

import com.numplates.nomera3.modules.bump.domain.repository.ShakeRepository
import javax.inject.Inject

class HasActiveShakeUsersUseCase @Inject constructor(
    private val repository: ShakeRepository
) {
    fun invoke(): Boolean = repository.hasActiveShakeUsers()
}
