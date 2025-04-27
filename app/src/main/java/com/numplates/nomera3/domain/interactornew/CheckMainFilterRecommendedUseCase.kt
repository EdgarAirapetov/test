package com.numplates.nomera3.domain.interactornew

import com.numplates.nomera3.modules.newroads.data.entities.FilterSettingsProvider
import javax.inject.Inject

class CheckMainFilterRecommendedUseCase @Inject constructor(
    private val filterSettingsProvider: FilterSettingsProvider
) {

    fun invoke(): Boolean {
        val filterSettings = filterSettingsProvider.get(FilterSettingsProvider.FilterType.Main)
        return filterSettings.isRecommended()
    }
}
