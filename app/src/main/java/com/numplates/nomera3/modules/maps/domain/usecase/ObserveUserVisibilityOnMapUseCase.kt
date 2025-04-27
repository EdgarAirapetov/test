package com.numplates.nomera3.modules.maps.domain.usecase

import com.numplates.nomera3.modules.maps.domain.repository.MapSettingsRepository
import com.numplates.nomera3.presentation.model.enums.SettingsUserTypeEnum
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveUserVisibilityOnMapUseCase @Inject constructor(
    private val repository: MapSettingsRepository
) {

    fun invoke(): Flow<SettingsUserTypeEnum> {
        return repository.observeUserVisibilityOnMapSetting()
    }
}
