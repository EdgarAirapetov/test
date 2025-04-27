package com.numplates.nomera3.modules.baseCore.data.repository

import com.meera.application_api.analytic.AmplitudeEventDelegate
import com.numplates.nomera3.modules.baseCore.domain.repository.AmplitudeShakeAnalyticRepository
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyNameConst
import com.meera.application_api.analytic.addProperty
import com.numplates.nomera3.modules.baseCore.helper.amplitude.shake.AmplitudeShakeEventName
import com.numplates.nomera3.modules.baseCore.helper.amplitude.shake.AmplitudeShakeHowProperty
import com.numplates.nomera3.modules.baseCore.helper.amplitude.shake.AmplitudeShakePositionProperty
import com.numplates.nomera3.modules.baseCore.helper.amplitude.shake.AmplitudeShakeWhereProperty
import javax.inject.Inject

class AmplitudeShakeAnalyticRepositoryImpl @Inject constructor(
    private val amplitudeEventDelegate: AmplitudeEventDelegate
) : AmplitudeShakeAnalyticRepository {

    override fun logShakeSwitchChanged(
        shakePositionProperty: AmplitudeShakePositionProperty,
        userId: Long
    ) {
        amplitudeEventDelegate.logEvent(
            eventName = AmplitudeShakeEventName.SHAKE_TOGGLE,
            properties = {
                it.apply {
                    addProperty(shakePositionProperty)
                    addProperty(AmplitudePropertyNameConst.USER_ID, userId)
                }
            }
        )
    }

    override fun logShakeResults(
        howCalled: AmplitudeShakeHowProperty,
        countMutualAudience: Int,
        countUserShake: Int,
        fromId: Long,
        toId: Long
    ) {
        amplitudeEventDelegate.logEvent(
            eventName = AmplitudeShakeEventName.SHAKE_RESULTS,
            properties = {
                it.apply {
                    addProperty(howCalled)
                    addProperty(AmplitudePropertyNameConst.COUNT_MUTUAL_AUDIENCE, countMutualAudience)
                    addProperty(AmplitudePropertyNameConst.COUNT_USER_SHAKE, countUserShake)
                    addProperty(AmplitudePropertyNameConst.FROM, fromId)
                    addProperty(AmplitudePropertyNameConst.TO, toId)
                }
            }
        )
    }

    override fun logShakeTap(
        howCalled: AmplitudeShakeHowProperty,
        userId: Long,
        whereProperty: AmplitudeShakeWhereProperty
    ) {
        amplitudeEventDelegate.logEvent(
            eventName = AmplitudeShakeEventName.SHAKE_TAP,
            properties = {
                it.apply {
                    addProperty(howCalled)
                    addProperty(AmplitudePropertyNameConst.USER_ID, userId)
                    addProperty(whereProperty)
                }
            }
        )
    }
}
