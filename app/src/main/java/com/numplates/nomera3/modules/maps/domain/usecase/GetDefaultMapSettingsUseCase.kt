package com.numplates.nomera3.modules.maps.domain.usecase

import com.numplates.nomera3.modules.maps.domain.events.usecase.GetDefaultEventFiltersUseCase
import com.numplates.nomera3.modules.maps.domain.model.MapMode
import com.numplates.nomera3.modules.maps.domain.model.MapSettingsModel
import javax.inject.Inject

class GetDefaultMapSettingsUseCase @Inject constructor(
    private val getDefaultEventFiltersUseCase: GetDefaultEventFiltersUseCase
) {
    operator fun invoke(): MapSettingsModel = MapSettingsModel(
        mapMode = MapMode.DAY,
        showMen = true,
        showWomen = true,
        showFriendsOnly = false,
        showPeople = true,
        showEvents = true,
        showFriends = true,
        eventFilters = getDefaultEventFiltersUseCase.invoke()
    )
}
