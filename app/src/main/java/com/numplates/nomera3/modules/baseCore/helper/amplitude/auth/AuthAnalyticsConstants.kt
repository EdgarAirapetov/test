package com.numplates.nomera3.modules.baseCore.helper.amplitude.auth

import com.meera.application_api.analytic.model.AmplitudeName
import com.meera.application_api.analytic.model.AmplitudeProperty

enum class AuthAmplitudeEventName(private val event: String) : AmplitudeName {

    CODE_REPEAT_REQUEST("code repeat request");

    override val eventName: String
        get() = event
}



enum class AmplitudeCodeRepeatRequestProperty(val property: String) : AmplitudeProperty {

    REG_TYPE("reg type"),
    COUNTRY_NUMBER("country number"),
    NUMBER("number"),
    EMAIL("email");

    override val _value: String
        get() = property

    override val _name: String
        get() = AuthAmplitudeEventName.CODE_REPEAT_REQUEST.eventName
}
