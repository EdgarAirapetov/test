package com.numplates.nomera3.modules.maps.domain.usecase

import com.numplates.nomera3.modules.maps.domain.repository.LocationRepository
import javax.inject.Inject

class StopReceivingLocationUpdatesUseCase @Inject constructor(
    private val locationRepository: LocationRepository
) {
    operator fun invoke() = locationRepository.stopReceivingLocationUpdates()
}
