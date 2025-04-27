package com.numplates.nomera3.modules.baseCore.helper.amplitude.domain.mapvisibilitysettings.usecase

import com.numplates.nomera3.modules.baseCore.BaseUseCaseNoSuspend
import com.numplates.nomera3.modules.baseCore.DefParams
import com.numplates.nomera3.modules.baseCore.helper.amplitude.data.repository.mapvisibilitysettings.MapVisibilitySettingsAnalyticsRepository
import javax.inject.Inject

class MapVisibilitySettingsAnalyticsChangeCountUseCase @Inject constructor(
    private val repository: MapVisibilitySettingsAnalyticsRepository
): BaseUseCaseNoSuspend<MapVisibilityBlacklistChangeCountParams, Unit> {

    override fun execute(params: MapVisibilityBlacklistChangeCountParams) {
        repository.changeCount(params)
    }
}

class MapVisibilityBlacklistChangeCountParams(
    val addCount: Int = 0,
    val removeCount: Int = 0,
    val setCount: Int = 0
) : DefParams()