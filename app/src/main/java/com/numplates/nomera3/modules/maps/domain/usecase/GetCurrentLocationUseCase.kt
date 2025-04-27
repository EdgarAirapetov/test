package com.numplates.nomera3.modules.maps.domain.usecase

import com.numplates.nomera3.modules.baseCore.domain.model.CoordinatesModel
import com.numplates.nomera3.modules.maps.domain.repository.LocationRepository
import javax.inject.Inject

class GetCurrentLocationUseCase @Inject constructor(
    private val repository: LocationRepository
) {
    suspend operator fun invoke(): CoordinatesModel? = repository.getCurrentLocation()
}
