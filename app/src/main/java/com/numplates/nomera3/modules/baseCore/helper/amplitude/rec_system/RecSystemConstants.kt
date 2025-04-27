package com.numplates.nomera3.modules.baseCore.helper.amplitude.rec_system

import com.meera.application_api.analytic.model.AmplitudeName
import com.meera.application_api.analytic.model.AmplitudeProperty
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyNameConst

enum class AmplitudeRecSystemEventName(
    private val event: String
) : AmplitudeName {
    REC_FEED_CHANGE("rec feed change");

    override val eventName: String
        get() = event
}

enum class AmplitudePropertyRecSystemChangeMethod(val property: String) : AmplitudeProperty {
    AUTOMATICALLY("automatically"),
    MANUALLY("manually");

    override val _value: String
        get() = property

    override val _name: String
        get() = AmplitudePropertyNameConst.HOW
}

enum class AmplitudePropertyRecSystemType(val property: String) : AmplitudeProperty {
    REC("rec"),
    CHRON("chron");

    override val _value: String
        get() = property

    override val _name: String
        get() = AmplitudePropertyNameConst.REC_FEED_CHANGE_TYPE
}
