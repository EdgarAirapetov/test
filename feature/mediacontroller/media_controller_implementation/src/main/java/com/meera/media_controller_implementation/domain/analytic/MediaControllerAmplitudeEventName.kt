package com.meera.media_controller_implementation.domain.analytic

import com.meera.application_api.analytic.model.AmplitudeName

internal enum class MediaControllerAmplitudeEventName(
    private val event: String
) : AmplitudeName {
    VIDEO_ALERT_ACTION("video alert action");

    override val eventName: String
        get() = event
}
