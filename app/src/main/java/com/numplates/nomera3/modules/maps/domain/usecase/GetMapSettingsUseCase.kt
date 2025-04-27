package com.numplates.nomera3.modules.maps.domain.usecase

import com.numplates.nomera3.modules.maps.domain.model.MapSettingsModel
import com.numplates.nomera3.modules.maps.domain.repository.MapSettingsRepository
import javax.inject.Inject

class GetMapSettingsUseCase @Inject constructor(
    private val repository: MapSettingsRepository
) {
    operator fun invoke(): MapSettingsModel = repository.getMapSettings()
}
