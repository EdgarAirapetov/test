package com.numplates.nomera3.modules.bump.domain.usecase

import com.numplates.nomera3.modules.bump.domain.repository.ShakeRepository
import javax.inject.Inject

/**
 * UseCase, который позволяет установить [com.numplates.nomera3.modules.bump.hardware.ShakeEventListener]
 */
class TryToRegisterShakeEventUseCase @Inject constructor(
    private val shakeRepository: ShakeRepository
) {
    suspend fun invoke(isRegisterShakeEvent: Boolean) =
        shakeRepository.tryToRegisterShakeEvent(isRegisterShakeEvent)
}
