package com.numplates.nomera3.domain.interactornew

import com.numplates.nomera3.modules.newroads.data.entities.FilterSettings
import com.numplates.nomera3.modules.newroads.data.entities.FilterSettingsProvider
import javax.inject.Inject

class GetMainFilterSettingsUseCase @Inject constructor(
    private val filterSettingsProvider: FilterSettingsProvider
) {

    fun invoke(): FilterSettings {
        return filterSettingsProvider.get(FilterSettingsProvider.FilterType.Main)
    }
}
