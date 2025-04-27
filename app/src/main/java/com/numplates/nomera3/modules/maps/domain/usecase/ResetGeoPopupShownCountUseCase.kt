package com.numplates.nomera3.modules.maps.domain.usecase

import com.numplates.nomera3.modules.maps.domain.repository.MapDataRepository
import javax.inject.Inject

class ResetGeoPopupShownCountUseCase @Inject constructor(
    private val repository: MapDataRepository
) {
    fun invoke() = repository.resetGeoPopupShownCount()
}
