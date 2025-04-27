package com.numplates.nomera3.modules.baseCore.helper.amplitude.search

import com.meera.application_api.analytic.AmplitudeEventDelegate
import com.meera.application_api.analytic.addProperty
import javax.inject.Inject

interface AmplitudeMainSearchAnalytics {
    fun logOpenMainSearch(where: AmplitudeSearchOpenWhereProperty)
}

class AmplitudeMainSearchAnalyticsImpl @Inject constructor(
    private val delegate: AmplitudeEventDelegate
) : AmplitudeMainSearchAnalytics {

    override fun logOpenMainSearch(where: AmplitudeSearchOpenWhereProperty) {
        delegate.logEvent(
            eventName = AmplitudeMainSearchEventName.SEARCH_OPEN,
            properties = {
                it.apply {
                    addProperty(where)
                }
            }
        )
    }


}
