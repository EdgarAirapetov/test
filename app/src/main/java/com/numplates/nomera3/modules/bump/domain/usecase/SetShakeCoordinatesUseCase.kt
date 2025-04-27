package com.numplates.nomera3.modules.bump.domain.usecase

import com.numplates.nomera3.modules.bump.domain.repository.ShakeRepository
import javax.inject.Inject

class SetShakeCoordinatesUseCase @Inject constructor(
    private val repository: ShakeRepository
) {
    suspend fun invoke(gpsX: Float, gpsY: Float): Any {
        return repository.setShakeCoordinates(
            gpsX = gpsX,
            gpsY = gpsY
        )
    }
}
