package com.numplates.nomera3.modules.baseCore.helper.amplitude.geo_popup

import com.meera.application_api.analytic.AmplitudeEventDelegate
import com.meera.application_api.analytic.addProperty
import javax.inject.Inject

interface AmplitudeGeoPopup {
    fun logGeoPopupAction(actionType: AmplitudePropertyGeoPopupActionType, where: AmplitudePropertyGeoPopupWhere)
}

class AmplitudeGeoPopupImpl @Inject constructor(
    private val delegate: AmplitudeEventDelegate
) : AmplitudeGeoPopup {

    override fun logGeoPopupAction(
        actionType: AmplitudePropertyGeoPopupActionType,
        where: AmplitudePropertyGeoPopupWhere
    ) {
        delegate.logEvent(
            eventName = AmplitudeGeoPopupEventName.GEO_POPUP_ACTION,
            properties = {
                it.apply {
                    addProperty(actionType)
                    addProperty(where)
                }
            }
        )
    }
}
