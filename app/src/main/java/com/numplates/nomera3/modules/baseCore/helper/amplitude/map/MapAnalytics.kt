package com.numplates.nomera3.modules.baseCore.helper.amplitude.map

import com.meera.application_api.analytic.AmplitudeEventDelegate
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyWhereOpenMap
import com.meera.application_api.analytic.addProperty
import javax.inject.Inject

interface AmplitudeMap {
    fun logBackToMyLocation()

    fun logOpenMap(where: AmplitudePropertyWhereOpenMap)
}

class AmplitudeMapImpl @Inject constructor(
    private val delegate: AmplitudeEventDelegate
) : AmplitudeMap {

    override fun logBackToMyLocation() {
        delegate.logEvent(
            eventName = AmplitudeMapEventName.FIND_ME
        )
    }

    override fun logOpenMap(where: AmplitudePropertyWhereOpenMap) {
        delegate.logEvent(
            eventName = AmplitudeMapEventName.MAP_OPEN,
            properties = {
                it.apply {
                    addProperty(where)
                }
            }
        )
    }
}
