package com.numplates.nomera3.modules.baseCore.helper.amplitude.screenshot

import com.meera.application_api.analytic.AmplitudeEventDelegate
import com.meera.application_api.analytic.addProperty
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudeUserPropertiesDelegate
import com.numplates.nomera3.modules.baseCore.helper.amplitude.PRIVACY_SCREENSHOT_SHARE_ACTION
import org.json.JSONObject
import javax.inject.Inject

private const val DEFAULT_ID = 0L

interface AmplitudeScreenshotAnalytics {

    fun logScreenshotShareOpen(
        whereProperty: AmplitudeScreenshotWhereProperty,
        fromId: Long,
        profileId: Long = DEFAULT_ID,
        eventId: Long = DEFAULT_ID,
        momentId: Long = DEFAULT_ID,
        postId: Long = DEFAULT_ID,
        communityId: Long = DEFAULT_ID
    )

    fun logScreenshotShareAction(
        actionTypeProperty: AmplitudeScreenshotActionTypeProperty,
        whereProperty: AmplitudeScreenshotWhereProperty,
        fromId: Long,
        profileId: Long = DEFAULT_ID,
        eventId: Long = DEFAULT_ID,
        momentId: Long = DEFAULT_ID,
        postId: Long = DEFAULT_ID,
        communityId: Long = DEFAULT_ID
    )

    fun logScreenshotShareTogglePress(
        positionProperty: AmplitudeScreenshotPositionProperty,
        fromId: Long
    )

    fun setUserPropertiesShareScreenshotChanged(isRecommended: Boolean)

}

class AmplitudeScreenshotAnalyticsImpl @Inject constructor(
    private val eventDelegate: AmplitudeEventDelegate,
    private val userPropertiesDelegate: AmplitudeUserPropertiesDelegate
) : AmplitudeScreenshotAnalytics {

    override fun logScreenshotShareOpen(
        whereProperty: AmplitudeScreenshotWhereProperty,
        fromId: Long,
        profileId: Long,
        eventId: Long,
        momentId: Long,
        postId: Long,
        communityId: Long
    ) {
        eventDelegate.logEvent(
            eventName = AmplitudeScreenshotEventName.SCREENSHOT_SHARE_OPEN,
            properties = {
                it.apply {
                    addProperty(whereProperty)
                    addProperty(AmplitudeScreenshotConstants.FROM, fromId)
                    addProperty(AmplitudeScreenshotConstants.PROFILE_ID, profileId)
                    addProperty(AmplitudeScreenshotConstants.EVENT_ID, eventId)
                    addProperty(AmplitudeScreenshotConstants.MOMENT_ID, momentId)
                    addProperty(AmplitudeScreenshotConstants.POST_ID, postId)
                    addProperty(AmplitudeScreenshotConstants.COMMUNITY_ID, communityId)
                }
            }
        )
    }

    override fun logScreenshotShareAction(
        actionTypeProperty: AmplitudeScreenshotActionTypeProperty,
        whereProperty: AmplitudeScreenshotWhereProperty,
        fromId: Long,
        profileId: Long,
        eventId: Long,
        momentId: Long,
        postId: Long,
        communityId: Long
    ) {
        eventDelegate.logEvent(
            eventName = AmplitudeScreenshotEventName.SCREENSHOT_SHARE_ACTION,
            properties = {
                it.apply {
                    addProperty(actionTypeProperty)
                    addProperty(whereProperty)
                    addProperty(AmplitudeScreenshotConstants.FROM, fromId)
                    addProperty(AmplitudeScreenshotConstants.PROFILE_ID, profileId)
                    addProperty(AmplitudeScreenshotConstants.EVENT_ID, eventId)
                    addProperty(AmplitudeScreenshotConstants.MOMENT_ID, momentId)
                    addProperty(AmplitudeScreenshotConstants.POST_ID, postId)
                    addProperty(AmplitudeScreenshotConstants.COMMUNITY_ID, communityId)
                }
            }
        )
    }

    override fun logScreenshotShareTogglePress(positionProperty: AmplitudeScreenshotPositionProperty, fromId: Long) {
        eventDelegate.logEvent(
            eventName = AmplitudeScreenshotEventName.SCREENSHOT_SHARE_TOGGLE_PRESS,
            properties = {
                it.apply {
                    addProperty(positionProperty)
                    addProperty(AmplitudeScreenshotConstants.FROM, fromId)
                }
            }
        )
    }

    override fun setUserPropertiesShareScreenshotChanged(isRecommended: Boolean) {
        userPropertiesDelegate.setUserProperties(
            JSONObject().apply {
                put(PRIVACY_SCREENSHOT_SHARE_ACTION, isRecommended)
            }
        )
    }
}
