package com.numplates.nomera3.modules.baseCore.helper.amplitude.screenshot

import com.meera.application_api.analytic.model.AmplitudeName
import com.meera.application_api.analytic.model.AmplitudeProperty

enum class AmplitudeScreenshotEventName(
    private val event: String
) : AmplitudeName {
    SCREENSHOT_SHARE_OPEN("screenshot share open"),
    SCREENSHOT_SHARE_ACTION("screenshot share action"),
    SCREENSHOT_SHARE_TOGGLE_PRESS("screenshot share toggle press");

    override val eventName: String
        get() = event

}

enum class AmplitudeScreenshotWhereProperty(
    private val value: String
) : AmplitudeProperty {
    OWN_PROFILE("own profile"),
    USER_PROFILE("user profile"),
    MAP_EVENT("map event"),
    POST_EVENT("post event"),
    MOMENT("moment"),
    FEED_POST("feed post"),
    COMMUNITY("community"),
    COMMUNITY_POST("community post"),
    OTHER("other");

    override val _value: String
        get() = value
    override val _name: String
        get() = AmplitudeScreenshotConstants.WHERE

}

enum class AmplitudeScreenshotActionTypeProperty(
    private val value: String
) : AmplitudeProperty {
    SHARE("share"),
    LINK("link"),
    CLOSE("close");


    override val _value: String
        get() = value
    override val _name: String
        get() = AmplitudeScreenshotConstants.ACTION_TYPE

}

enum class AmplitudeScreenshotPositionProperty(
    private val value: String
) : AmplitudeProperty {
    ON("on"),
    OFF("off");


    override val _value: String
        get() = value
    override val _name: String
        get() = AmplitudeScreenshotConstants.POSITION
}



object AmplitudeScreenshotConstants {
    const val WHERE = "where"
    const val FROM = "from"
    const val PROFILE_ID = "profile id"
    const val EVENT_ID = "event id"
    const val POST_ID = "post id"
    const val MOMENT_ID = "moment id"
    const val COMMUNITY_ID = "community id"
    const val ACTION_TYPE = "action type"
    const val POSITION = "position"
}
