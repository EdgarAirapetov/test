package com.numplates.nomera3.modules.baseCore.domain.repository

import com.numplates.nomera3.modules.baseCore.helper.amplitude.shake.AmplitudeShakeHowProperty
import com.numplates.nomera3.modules.baseCore.helper.amplitude.shake.AmplitudeShakePositionProperty
import com.numplates.nomera3.modules.baseCore.helper.amplitude.shake.AmplitudeShakeWhereProperty

interface AmplitudeShakeAnalyticRepository {

    fun logShakeSwitchChanged(
        shakePositionProperty: AmplitudeShakePositionProperty,
        userId: Long
    )

    fun logShakeResults(
        howCalled: AmplitudeShakeHowProperty,
        countMutualAudience: Int,
        countUserShake: Int,
        fromId: Long,
        toId: Long
    )

    fun logShakeTap(
        howCalled: AmplitudeShakeHowProperty,
        userId: Long,
        where: AmplitudeShakeWhereProperty
    )
}
