package com.numplates.nomera3.modules.bump.domain.usecase

import com.numplates.nomera3.modules.bump.domain.repository.ShakeRepository
import javax.inject.Inject

class EmitShakeUserNotFoundUseCase @Inject constructor(
    private val repository: ShakeRepository
) {
    suspend fun invoke() = repository.emitShakeUsersNotFound()
}
