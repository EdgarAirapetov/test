package com.numplates.nomera3.modules.baseCore.helper.amplitude.map_filters

import com.meera.application_api.analytic.AmplitudeEventDelegate
import com.meera.application_api.analytic.addProperty
import javax.inject.Inject

interface AmplitudeMapFilters {
    fun onMapFiltersShowFriendsOnlyPressed(enabled: Boolean)
    fun onMapFiltersShowMenPressed(enabled: Boolean)
    fun onMapFiltersShowWomenPressed(enabled: Boolean)
    fun logMapFilterApply(settingsChanged: Boolean)
    fun onMapFiltersClosed(
        haveChanges: Boolean,
        visibility: AmplitudePropertyMapFiltersVisibility,
        peopleEnabled: Boolean,
        eventsEnabled: Boolean,
        friendsEnabled: Boolean
    )
}

class AmplitudeHelperMapFiltersImpl @Inject constructor(
    private val delegate: AmplitudeEventDelegate
) : AmplitudeMapFilters {

    override fun onMapFiltersShowFriendsOnlyPressed(enabled: Boolean) {
        onMapFiltersSettingPressed(AmplitudeMapFiltersEventName.MAP_FILTERS_SHOW_FRIENDS_ONLY_PRESSED, enabled)
    }

    override fun onMapFiltersShowMenPressed(enabled: Boolean) {
        onMapFiltersSettingPressed(AmplitudeMapFiltersEventName.MAP_FILTERS_SHOW_MEN_PRESSED, enabled)
    }

    override fun onMapFiltersShowWomenPressed(enabled: Boolean) {
        onMapFiltersSettingPressed(AmplitudeMapFiltersEventName.MAP_FILTERS_SHOW_WOMEN_PRESSED, enabled)
    }

    override fun logMapFilterApply(settingsChanged: Boolean) {
        val changes = if (settingsChanged) AmplitudePropertyMapFiltersHaveChanges.YES
        else AmplitudePropertyMapFiltersHaveChanges.NO
        delegate.logEvent(
            eventName = AmplitudeMapFiltersEventName.MAP_FILTER_APPLY,
            properties = {
                it.apply {
                    addProperty(changes)
                }
            }
        )
    }

    override fun onMapFiltersClosed(
        haveChanges: Boolean,
        visibility: AmplitudePropertyMapFiltersVisibility,
        peopleEnabled: Boolean,
        eventsEnabled: Boolean,
        friendsEnabled: Boolean
    ) {
        delegate.logEvent(
            eventName = AmplitudeMapFiltersEventName.MAP_FILTER_CLOSED,
            properties = {
                it.apply {
                    addProperty(
                        propertyName = AmplitudePropertyMapFiltersConst.HAVE_CHANGES,
                        value = haveChanges
                    )
                    addProperty(visibility)
                    addProperty(
                        propertyName = AmplitudePropertyMapFiltersConst.PEOPLE_FILTER,
                        value = peopleEnabled
                    )
                    addProperty(
                        propertyName = AmplitudePropertyMapFiltersConst.EVENTS_FILTER,
                        value = eventsEnabled
                    )
                    addProperty(
                        propertyName = AmplitudePropertyMapFiltersConst.FRIENDS_FILTER,
                        value = friendsEnabled
                    )
                }
            }
        )
    }

    private fun onMapFiltersSettingPressed(eventName: AmplitudeMapFiltersEventName, enabled: Boolean) {
        val settingState = AmplitudePropertyMapFiltersSettingState.valueOf(enabled)
        delegate.logEvent(
            eventName = eventName,
            properties = {
                it.apply {
                    addProperty(settingState)
                }
            }
        )
    }
}
