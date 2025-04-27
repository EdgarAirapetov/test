package com.numplates.nomera3.modules.baseCore.helper.amplitude.map_filters

import com.meera.application_api.analytic.model.AmplitudeName
import com.meera.application_api.analytic.model.AmplitudeProperty
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyNameConst

enum class AmplitudeMapFiltersEventName(
    private val event: String
) : AmplitudeName {
    MAP_FILTERS_SHOW_FRIENDS_ONLY_PRESSED("show friends on map toggle press"),
    MAP_FILTERS_SHOW_MEN_PRESSED("show men on map toggle press"),
    MAP_FILTERS_SHOW_WOMEN_PRESSED("show women on map toggle press"),
    MAP_FILTER_APPLY("map filter open"),
    MAP_FILTER_CLOSED("map filter closed");

    override val eventName: String
        get() = event
}

enum class AmplitudePropertyMapFiltersSettingState(val property: String): AmplitudeProperty {
    ON("on"),
    OFF("off");

    override val _value: String
        get() = property

    override val _name: String
        get() = AmplitudePropertyMapFiltersConst.POSITION

    companion object {
        fun valueOf(value: Boolean): AmplitudePropertyMapFiltersSettingState {
            return if (value) ON else OFF
        }
    }
}

enum class AmplitudePropertyMapFiltersHaveChanges(val property: String) : AmplitudeProperty {
    YES("true"),
    NO("false");

    override val _name: String
        get() = AmplitudePropertyNameConst.HAS_CHANGES

    override val _value: String
        get() = property
}

enum class AmplitudePropertyMapFiltersVisibility(val property: String) : AmplitudeProperty {
    NOBODY("nobody"),
    FRIENDS("friends"),
    ALL("all");

    override val _value: String
        get() = property

    override val _name: String
        get() = AmplitudePropertyMapFiltersConst.VISIBILITY
}

object AmplitudePropertyMapFiltersConst {
    const val POSITION = "position"
    const val HAVE_CHANGES = "have changes"
    const val PEOPLE_FILTER = "people filter"
    const val EVENTS_FILTER = "events filter"
    const val FRIENDS_FILTER = "friends filter"
    const val VISIBILITY = "visibility"
}
