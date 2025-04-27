package com.numplates.nomera3.modules.baseCore.helper.amplitude.data.repository.mapvisibilitysettings

import com.numplates.nomera3.modules.baseCore.helper.amplitude.domain.mapvisibilitysettings.usecase.MapVisibilityBlacklistChangeCountParams
import com.numplates.nomera3.modules.baseCore.helper.amplitude.domain.mapvisibilitysettings.usecase.MapVisibilityBlacklistInitParams
import com.numplates.nomera3.modules.baseCore.helper.amplitude.domain.mapvisibilitysettings.usecase.MapVisibilityBlacklistLogDataParams

interface MapVisibilitySettingsAnalyticsRepository {

    fun init(params: MapVisibilityBlacklistInitParams)

    fun changeCount(params: MapVisibilityBlacklistChangeCountParams)

    fun log(params: MapVisibilityBlacklistLogDataParams)
}