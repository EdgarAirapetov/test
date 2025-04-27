package com.numplates.nomera3.modules.maps.domain.usecase

import com.numplates.nomera3.modules.maps.domain.model.MapSettingsModel
import com.numplates.nomera3.modules.maps.domain.repository.MapSettingsRepository
import javax.inject.Inject

class SetMapSettingsUseCase @Inject constructor(
    private val repository: MapSettingsRepository
) {
    operator fun invoke(settings: MapSettingsModel) = repository.setMapSettings(settings)
}
