package com.numplates.nomera3.domain.interactornew

import com.numplates.nomera3.modules.newroads.data.entities.FilterSettingsProvider
import com.numplates.nomera3.modules.newroads.util.FilterSettingsMapper
import javax.inject.Inject

class GetCitiesForMainFilterUseCase @Inject constructor(
    private val filterSettingsProvider: FilterSettingsProvider,
    private val filterMapper: FilterSettingsMapper
) {
    fun invoke(): String {
        val filterSettings = filterSettingsProvider.get(FilterSettingsProvider.FilterType.Main)
        return filterMapper.mapToCityIds(filterSettings)
    }
}
