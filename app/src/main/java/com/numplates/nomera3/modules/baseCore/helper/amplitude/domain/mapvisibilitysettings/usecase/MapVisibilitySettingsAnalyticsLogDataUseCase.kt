package com.numplates.nomera3.modules.baseCore.helper.amplitude.domain.mapvisibilitysettings.usecase

import com.numplates.nomera3.modules.baseCore.BaseUseCaseNoSuspend
import com.numplates.nomera3.modules.baseCore.DefParams
import com.numplates.nomera3.modules.baseCore.helper.amplitude.data.repository.mapvisibilitysettings.MapVisibilitySettingsAnalyticsRepository
import javax.inject.Inject

class MapVisibilitySettingsAnalyticsLogDataUseCase @Inject constructor(
    private val repository: MapVisibilitySettingsAnalyticsRepository
): BaseUseCaseNoSuspend<MapVisibilityBlacklistLogDataParams, Unit> {

    override fun execute(params: MapVisibilityBlacklistLogDataParams) {
        repository.log(params)
    }
}

class MapVisibilityBlacklistLogDataParams(
    val deleteAll: Boolean
) : DefParams()