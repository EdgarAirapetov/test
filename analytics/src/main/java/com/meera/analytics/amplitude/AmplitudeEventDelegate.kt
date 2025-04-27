package com.meera.analytics.amplitude

import com.amplitude.BuildConfig
import com.amplitude.api.AmplitudeClient
import com.amplitude.api.Identify
import com.yandex.metrica.YandexMetrica
import org.json.JSONObject
import timber.log.Timber
import javax.inject.Inject

class AmplitudeEventDelegate @Inject constructor(
    private val client: AmplitudeClient
) {

    fun logEvent(eventName: AmplitudeName, properties: (JSONObject) -> JSONObject = { JSONObject() }) {
        val preparedProperties = properties(JSONObject())
        val isDebug = BuildConfig.DEBUG
        Timber.d("(CORE) Handle event: isDebug = $isDebug  event = ${eventName.eventName}, property: $preparedProperties")
        if (!isDebug) {
            client.logEvent(eventName.eventName, preparedProperties)
            YandexMetrica.reportEvent(eventName.eventName, preparedProperties.toString())
        }
    }
}

fun JSONObject.addProperty(propertyName: String, value: Any) {
    put(propertyName, value)
}

fun Identify.setProperty(property: AmplitudeProperty) {
    set(property._name, property._value)
}

fun JSONObject.addProperty(propertyName: AmplitudeProperty, value: AmplitudeProperty) {
    put(propertyName._value, value._name)
}

fun JSONObject.addProperty(property: AmplitudeProperty) {
    put(property._name, property._value)
}
