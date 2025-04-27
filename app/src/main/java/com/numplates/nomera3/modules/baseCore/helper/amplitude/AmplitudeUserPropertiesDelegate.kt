package com.numplates.nomera3.modules.baseCore.helper.amplitude

import com.amplitude.api.AmplitudeClient
import com.numplates.nomera3.BuildConfig
import org.json.JSONObject
import timber.log.Timber
import javax.inject.Inject

class AmplitudeUserPropertiesDelegate @Inject constructor(
    private val client: AmplitudeClient
) {

    fun setUserProperties(properties: JSONObject) {
        val isDebug = BuildConfig.DEBUG
        Timber.d("Handle user properties: isDebug = $isDebug, property: $properties")
        if (!isDebug) {
            client.setUserProperties(properties)
        }
    }
}
