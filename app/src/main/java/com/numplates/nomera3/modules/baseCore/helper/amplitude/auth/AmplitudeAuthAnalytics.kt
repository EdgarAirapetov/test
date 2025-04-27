package com.numplates.nomera3.modules.baseCore.helper.amplitude.auth

import com.meera.application_api.analytic.AmplitudeEventDelegate
import com.meera.application_api.analytic.addProperty
import javax.inject.Inject

interface AmplitudeAuthAnalytic {
    fun codeRepeatRequest(regType:String,countryNumber:String?,number:String,email:String)
}


class AmplitudeAuthAnalyticImpl @Inject constructor(
    private val delegate: AmplitudeEventDelegate
) : AmplitudeAuthAnalytic {
    override fun codeRepeatRequest(regType:String,countryNumber:String?,number:String,email:String) {
        delegate.logEvent(
            eventName = AuthAmplitudeEventName.CODE_REPEAT_REQUEST,
            properties = {
                it.apply {
                    addProperty(AmplitudeCodeRepeatRequestProperty.REG_TYPE.property, regType)
                    addProperty(AmplitudeCodeRepeatRequestProperty.COUNTRY_NUMBER.property, countryNumber?:"")
                    addProperty(AmplitudeCodeRepeatRequestProperty.NUMBER.property, number)
                    addProperty(AmplitudeCodeRepeatRequestProperty.EMAIL.property, email)

                }
            }
        )
    }
}
