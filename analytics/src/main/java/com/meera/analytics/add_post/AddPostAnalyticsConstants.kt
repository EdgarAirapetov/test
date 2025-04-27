package com.meera.analytics.add_post

import com.meera.analytics.amplitude.AmplitudeName

// events names
private const val EVENT_POST_BACKGROUND_TAP = "post background tap"

// properties names
private const val PROPERTY_USER_ID = "user id"

enum class AmplitudeAddPostEventName(
    val event: String
) : AmplitudeName {

    POST_BACKGROUND_TAP(EVENT_POST_BACKGROUND_TAP);

    override val eventName: String
        get() = event
}

object AmplitudeAddPostPropertyNameConst {
    const val USER_ID = PROPERTY_USER_ID
}
