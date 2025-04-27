package com.numplates.nomera3.modules.maps.domain.repository

interface MapTooltipsRepository {
    fun needToShowLayersTooltipPeopleDisabled(): Boolean
    fun needToShowLayersTooltipEventsDisabled(): Boolean
    fun needToShowLayersTooltipFriendsDisabled(): Boolean
    fun needToShowLayersTooltipTwoLayersDisabled(): Boolean
    fun needToShowLayersTooltipAllLayersDisabled(): Boolean
    fun needToShowLayersTooltipOnboarding(): Boolean

    fun writeLayersTooltipPeopleDisabledShown()
    fun writeLayersTooltipEventsDisabledShown()
    fun writeLayersTooltipFriendsDisabledShown()
    fun writeLayersTooltipTwoLayersDisabledShown()
    fun writeLayersTooltipAllLayersDisabledShown()
    fun writeLayersTooltipOnboardingShown()
}
