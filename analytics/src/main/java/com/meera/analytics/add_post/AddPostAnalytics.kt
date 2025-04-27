package com.meera.analytics.add_post

import com.meera.analytics.amplitude.AmplitudeEventDelegate
import com.meera.analytics.amplitude.addProperty
import javax.inject.Inject

interface AddPostAnalytics {

    fun onTapPostBackground(userId: Long)
}

class AddPostAnalyticsImpl @Inject constructor(
    private val delegate: AmplitudeEventDelegate
) : AddPostAnalytics {

    override fun onTapPostBackground(userId: Long) {
        delegate.logEvent(
            eventName = AmplitudeAddPostEventName.POST_BACKGROUND_TAP,
            properties = {
                it.apply {
                    addProperty(AmplitudeAddPostPropertyNameConst.USER_ID, userId)
                }
            }
        )
    }
}
