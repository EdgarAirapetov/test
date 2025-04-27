package com.numplates.nomera3.modules.baseCore.helper.amplitude.profilestatistics

import com.meera.application_api.analytic.model.AmplitudeName
import com.meera.application_api.analytic.model.AmplitudeProperty

enum class AmplitudeProfileStatisticsEventName(
    private val event: String
) : AmplitudeName {
    INTRO("stat1 welcome"),
    VISITORS("stat2 profile visitors"),
    VIEWS("stat3 content views"),
    CLOSE("stat screen close"),
    BUTTON_POST_CREATE_TAP("button post create tap");

    override val eventName: String
        get() = event
}

enum class AmplitudePropertyProfileStatisticsVisitorsTrendType(
    val type: String
) : AmplitudeProperty {
    POSITIVE("positive"),
    NEGATIVE("negative"),
    NEUTRAL("neutral");

    override val _value: String
        get() = type
    override val _name: String
        get() = AmplitudePropertyProfileStatisticsConst.VISITORS_TREND_TYPE
}

enum class AmplitudePropertyProfileStatisticsViewsTrendType(
    val type: String
) : AmplitudeProperty {
    POSITIVE("positive"),
    NEGATIVE("negative"),
    NEUTRAL("neutral");

    override val _value: String
        get() = type
    override val _name: String
        get() = AmplitudePropertyProfileStatisticsConst.VIEWS_TREND_TYPE
}

enum class AmplitudePropertyProfileStatisticsCloseType(
    val type: String
) : AmplitudeProperty {
    CLOSE("close"),
    CLOSE_SWIPE("close swipe"),
    TAP("tap"),
    BUTTON("button");

    override val _value: String
        get() = type
    override val _name: String
        get() = AmplitudePropertyProfileStatisticsConst.CLOSE_TYPE

}

object AmplitudePropertyProfileStatisticsConst {
    const val VISITORS_TREND_TYPE = "visitors trend"
    const val VIEWS_TREND_TYPE = "views trend"

    const val CLOSE_TYPE = "close type"

    const val PROPERTY_USER_ID = "user id"
    const val PROPERTY_WEEK = "week"
    const val PROPERTY_YEAR = "year"
    const val PROPERTY_VISITORS_COUNT = "amount visitors"
    const val PROPERTY_VIEWS_COUNT = "amount views"
    const val PROPERTY_VISITOR_GROWTH = "visitor growth"
    const val PROPERTY_VIEW_GROWTH = "view growth"
    const val PROPERTY_SCREEN_NUMBER = "screen number"
}
