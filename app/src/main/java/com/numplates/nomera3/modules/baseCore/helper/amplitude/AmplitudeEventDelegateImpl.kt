package com.numplates.nomera3.modules.baseCore.helper.amplitude

import com.amplitude.api.AmplitudeClient
import com.meera.application_api.analytic.AmplitudeEventDelegate
import com.meera.application_api.analytic.model.AmplitudeName
import com.numplates.nomera3.BuildConfig
import com.yandex.metrica.YandexMetrica
import javax.inject.Inject
import org.json.JSONObject
import timber.log.Timber

class AmplitudeEventDelegateImpl @Inject constructor(
    private val client: AmplitudeClient
) : AmplitudeEventDelegate {

    override fun logEvent(eventName: AmplitudeName, properties: (JSONObject) -> JSONObject) {
        val preparedProperties = properties(JSONObject())
        val isDebug = BuildConfig.DEBUG
        Timber.d("Handle event: isDebug = $isDebug  event = ${eventName.eventName}, property: $preparedProperties")
        if (!isDebug) {
            client.logEvent(eventName.eventName, preparedProperties)
            YandexMetrica.reportEvent(eventName.eventName, preparedProperties.toString())
        }
    }
}
