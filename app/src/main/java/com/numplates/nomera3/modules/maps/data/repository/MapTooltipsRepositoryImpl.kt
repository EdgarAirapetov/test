package com.numplates.nomera3.modules.maps.data.repository

import com.meera.core.di.scopes.AppScope
import com.meera.core.preferences.AppSettings
import com.numplates.nomera3.modules.maps.domain.repository.MapTooltipsRepository
import javax.inject.Inject

@AppScope
class MapTooltipsRepositoryImpl @Inject constructor(
    private val appSettings: AppSettings,
) : MapTooltipsRepository {

    private var layersTooltipPeopleDisabledShown = false
    private var layersTooltipEventsDisabledShown = false
    private var layersTooltipFriendsDisabledShown = false
    private var layersTooltipTwoLayersDisabledShown = false
    private var layersTooltipAllLayersDisabledShown = false
    private var layersTooltipOnboardingShown = false

    override fun needToShowLayersTooltipPeopleDisabled(): Boolean =
        appSettings.readLayersTooltipPeopleDisabledShownTimes() < TOOLTIP_SHOWN_TIMES_MAX
            && layersTooltipPeopleDisabledShown.not()
    override fun needToShowLayersTooltipEventsDisabled(): Boolean =
        appSettings.readLayersTooltipEventsDisabledShownTimes() < TOOLTIP_SHOWN_TIMES_MAX
            && layersTooltipEventsDisabledShown.not()
    override fun needToShowLayersTooltipFriendsDisabled(): Boolean =
        appSettings.readLayersTooltipFriendsDisabledShownTimes() < TOOLTIP_SHOWN_TIMES_MAX
            && layersTooltipFriendsDisabledShown.not()
    override fun needToShowLayersTooltipTwoLayersDisabled(): Boolean =
        appSettings.readLayersTooltipTwoLayersDisabledShownTimes() < TOOLTIP_SHOWN_TIMES_MAX
            && layersTooltipTwoLayersDisabledShown.not()
    override fun needToShowLayersTooltipAllLayersDisabled(): Boolean =
        appSettings.readLayersTooltipAllLayersDisabledShownTimes() < TOOLTIP_SHOWN_TIMES_MAX
            && layersTooltipAllLayersDisabledShown.not()
    override fun needToShowLayersTooltipOnboarding(): Boolean =
        appSettings.readLayersTooltipOnboardingShownTimes() < TOOLTIP_SHOWN_TIMES_MAX
            && layersTooltipOnboardingShown.not()

    override fun writeLayersTooltipPeopleDisabledShown() {
        layersTooltipPeopleDisabledShown = true
        appSettings.writeLayersTooltipPeopleDisabledShown()
    }
    override fun writeLayersTooltipEventsDisabledShown() {
        layersTooltipEventsDisabledShown = true
        appSettings.writeLayersTooltipEventsDisabledShown()
    }
    override fun writeLayersTooltipFriendsDisabledShown() {
        layersTooltipFriendsDisabledShown = true
        appSettings.writeLayersTooltipFriendsDisabledShown()
    }
    override fun writeLayersTooltipTwoLayersDisabledShown() {
        layersTooltipTwoLayersDisabledShown = true
        appSettings.writeLayersTooltipTwoLayersDisabledShown()
    }
    override fun writeLayersTooltipAllLayersDisabledShown() {
        layersTooltipAllLayersDisabledShown = true
        appSettings.writeLayersTooltipAllLayersDisabledShown()
    }
    override fun writeLayersTooltipOnboardingShown() {
        layersTooltipOnboardingShown = true
        appSettings.writeLayersTooltipOnboardingShown()
    }

    companion object {
        private const val TOOLTIP_SHOWN_TIMES_MAX = 3
    }
}
