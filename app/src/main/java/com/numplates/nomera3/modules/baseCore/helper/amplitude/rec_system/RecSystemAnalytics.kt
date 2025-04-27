package com.numplates.nomera3.modules.baseCore.helper.amplitude.rec_system

import com.meera.application_api.analytic.AmplitudeEventDelegate
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyNameConst
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudeUserPropertiesDelegate
import com.numplates.nomera3.modules.baseCore.helper.amplitude.REC_SYSTEM_ACTION
import com.meera.application_api.analytic.addProperty
import org.json.JSONObject
import javax.inject.Inject

interface AmplitudeRecSystemAnalytics {
    fun logRecSystemChanged(
        how: AmplitudePropertyRecSystemChangeMethod,
        type: AmplitudePropertyRecSystemType,
        userId: Long
    )

    fun setUserPropertiesRecSystemChanged(isRecommended: Boolean)
}

class AmplitudeRecSystemAnalyticsImpl @Inject constructor(
    private val eventDelegate: AmplitudeEventDelegate,
    private val userPropertiesDelegate: AmplitudeUserPropertiesDelegate
) : AmplitudeRecSystemAnalytics {
    override fun logRecSystemChanged(
        how: AmplitudePropertyRecSystemChangeMethod,
        type: AmplitudePropertyRecSystemType,
        userId: Long) {
        eventDelegate.logEvent(
            eventName = AmplitudeRecSystemEventName.REC_FEED_CHANGE,
            properties = {
                it.apply {
                    addProperty(AmplitudePropertyNameConst.HOW, how._value)
                    addProperty(AmplitudePropertyNameConst.REC_FEED_CHANGE_TYPE, type._value)
                    addProperty(AmplitudePropertyNameConst.USER_ID, userId)
                }
            }
        )
    }

    override fun setUserPropertiesRecSystemChanged(isRecommended: Boolean) {
        userPropertiesDelegate.setUserProperties(
            JSONObject().apply {
                put(REC_SYSTEM_ACTION, isRecommended)
            }
        )
    }
}
