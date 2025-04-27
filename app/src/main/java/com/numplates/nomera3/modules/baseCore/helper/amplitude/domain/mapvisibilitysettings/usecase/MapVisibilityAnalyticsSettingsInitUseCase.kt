package com.numplates.nomera3.modules.baseCore.helper.amplitude.domain.mapvisibilitysettings.usecase

import com.numplates.nomera3.modules.baseCore.BaseUseCaseNoSuspend
import com.numplates.nomera3.modules.baseCore.DefParams
import com.numplates.nomera3.modules.baseCore.helper.amplitude.data.repository.mapvisibilitysettings.MapVisibilitySettingsAnalyticsRepository
import com.numplates.nomera3.modules.baseCore.helper.amplitude.domain.mapvisibilitysettings.model.MapVisibilitySettingsListType
import com.numplates.nomera3.modules.maps.ui.entity.MapVisibilitySettingsOrigin
import javax.inject.Inject

class MapVisibilityAnalyticsSettingsInitUseCase @Inject constructor(
    private val repository: MapVisibilitySettingsAnalyticsRepository
): BaseUseCaseNoSuspend<MapVisibilityBlacklistInitParams, Unit> {

    override fun execute(params: MapVisibilityBlacklistInitParams) {
        repository.init(params)
    }
}

class MapVisibilityBlacklistInitParams(
    val origin: MapVisibilitySettingsOrigin?,
    val listType: MapVisibilitySettingsListType
) : DefParams()