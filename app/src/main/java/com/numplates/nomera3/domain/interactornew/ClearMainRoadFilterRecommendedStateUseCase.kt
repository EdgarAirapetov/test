package com.numplates.nomera3.domain.interactornew

import com.numplates.nomera3.modules.newroads.data.entities.FilterSettingsProvider
import javax.inject.Inject

class ClearMainRoadFilterRecommendedStateUseCase @Inject constructor(
    private val filterSettingsProvider: FilterSettingsProvider
) {
    fun invoke() {
        val filterSettings = filterSettingsProvider.get(FilterSettingsProvider.FilterType.Main)
        filterSettings.data.isRecommended = null
        filterSettings.commit()
    }
}
