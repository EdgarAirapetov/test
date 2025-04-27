package com.meera.application_api.analytic

import com.meera.application_api.analytic.model.AmplitudeProperty
import org.json.JSONObject

fun JSONObject.addProperty(propertyName: String, value: Any) {
    put(propertyName, value)
}

fun JSONObject.addProperty(propertyName: AmplitudeProperty, value: AmplitudeProperty) {
    put(propertyName._value, value._name)
}

fun JSONObject.addProperty(property: AmplitudeProperty) {
    put(property._name, property._value)
}
