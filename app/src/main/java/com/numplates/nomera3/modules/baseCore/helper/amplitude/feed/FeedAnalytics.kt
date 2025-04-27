package com.numplates.nomera3.modules.baseCore.helper.amplitude.feed

import com.meera.application_api.analytic.AmplitudeEventDelegate
import com.meera.application_api.analytic.addProperty
import javax.inject.Inject

interface AmplitudeFeedAnalytics {
    fun logAnnouncementButtonPress(
        type: AmplitudeAnnouncementButtonType,
        haveAction: Boolean,
        announceName: String,
    )

    fun logVideoAlertAction(
        actionType: AmplitudeVideoAlertActionType,
        originalDuration: Long,
        maxDuration: Long,
    )
}

class AmplitudeFeedAnalyticsImpl @Inject constructor(
    private val delegate: AmplitudeEventDelegate
) : AmplitudeFeedAnalytics {

    override fun logAnnouncementButtonPress(
        type: AmplitudeAnnouncementButtonType,
        haveAction: Boolean,
        announceName: String,
    ) {
        delegate.logEvent(
            eventName = AmplitudeFeedEventName.ANNOUNCEMENT_BUTTON_PRESS,
            properties = {
                it.apply {
                    addProperty(type)
                    addProperty(HAVE_ACTION, haveAction)
                    addProperty(ANNOUNCE_NAME, announceName)
                }
            }
        )
    }

    override fun logVideoAlertAction(
        actionType: AmplitudeVideoAlertActionType,
        originalDuration: Long,
        maxDuration: Long
    ) {
        delegate.logEvent(
            eventName = AmplitudeFeedEventName.VIDEO_ALERT_ACTION,
            properties = {
                it.apply {
                    addProperty(actionType)
                    addProperty(ORIGINAL_VIDEO_DURATION, originalDuration)
                    addProperty(VIDEO_DURATION_MAX, maxDuration)
                }
            }
        )
    }
}
