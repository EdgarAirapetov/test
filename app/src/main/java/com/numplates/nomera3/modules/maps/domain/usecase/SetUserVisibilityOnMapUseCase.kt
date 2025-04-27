package com.numplates.nomera3.modules.maps.domain.usecase

import com.numplates.nomera3.modules.maps.domain.repository.MapSettingsRepository
import com.numplates.nomera3.presentation.model.enums.SettingsUserTypeEnum
import javax.inject.Inject

class SetUserVisibilityOnMapUseCase @Inject constructor(
    private val repository: MapSettingsRepository
) {
    operator fun invoke(typeEnum: SettingsUserTypeEnum) = repository.setUserVisibilityOnMapSetting(typeEnum)
}
