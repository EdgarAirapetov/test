package com.meera.application_api.analytic

import com.meera.application_api.analytic.model.AmplitudeName
import org.json.JSONObject

interface AmplitudeEventDelegate {
    fun logEvent(eventName: AmplitudeName, properties: (JSONObject) -> JSONObject = { JSONObject() })
}
