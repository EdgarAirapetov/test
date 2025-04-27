package com.meera.media_controller_implementation.domain.analytic

import com.meera.application_api.analytic.AmplitudeEventDelegate
import com.meera.application_api.analytic.addProperty
import javax.inject.Inject

internal class MediaControllerAmplitudeImpl @Inject constructor(
    private val delegate: AmplitudeEventDelegate
) : MediaControllerAmplitude {

    override fun logVideoAlertAction(
        actionType: MediaControllerAmplitudePropertyVideoAlertActionType,
        videoDurationSec: Int,
        maxVideoDurationSec: Int
    ) {
        delegate.logEvent(
            eventName = MediaControllerAmplitudeEventName.VIDEO_ALERT_ACTION,
            properties = {
                it.apply {
                    addProperty(actionType)
                    addProperty(MediaControllerAmplitudePropertyNameConst.VIDEO_ALERT_VIDEO_DURATION, videoDurationSec)
                    addProperty(MediaControllerAmplitudePropertyNameConst.VIDEO_ALERT_VIDEO_DURATION_MAX, maxVideoDurationSec)
                }
            }
        )
    }
}

