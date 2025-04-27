package com.numplates.nomera3.modules.maps.domain.interactor

import com.numplates.nomera3.modules.maps.domain.repository.MapTooltipsRepository
import javax.inject.Inject

class MapLayersTooltipsInteractor @Inject constructor(
    private val repository: MapTooltipsRepository
) {
    fun needToShowLayersTooltipPeopleDisabled(): Boolean = repository.needToShowLayersTooltipPeopleDisabled()
    fun needToShowLayersTooltipEventsDisabled(): Boolean = repository.needToShowLayersTooltipEventsDisabled()
    fun needToShowLayersTooltipFriendsDisabled(): Boolean = repository.needToShowLayersTooltipFriendsDisabled()
    fun needToShowLayersTooltipTwoLayersDisabled(): Boolean = repository.needToShowLayersTooltipTwoLayersDisabled()
    fun needToShowLayersTooltipAllLayersDisabled(): Boolean = repository.needToShowLayersTooltipAllLayersDisabled()
    fun needToShowLayersTooltipOnboarding(): Boolean = repository.needToShowLayersTooltipOnboarding()

    fun writeLayersTooltipPeopleDisabledShown() = repository.writeLayersTooltipPeopleDisabledShown()
    fun writeLayersTooltipEventsDisabledShown() = repository.writeLayersTooltipEventsDisabledShown()
    fun writeLayersTooltipFriendsDisabledShown() = repository.writeLayersTooltipFriendsDisabledShown()
    fun writeLayersTooltipTwoLayersDisabledShown() = repository.writeLayersTooltipTwoLayersDisabledShown()
    fun writeLayersTooltipAllLayersDisabledShown() = repository.writeLayersTooltipAllLayersDisabledShown()
    fun writeLayersTooltipOnboardingShown() = repository.writeLayersTooltipOnboardingShown()
}
