package com.numplates.nomera3.modules.baseCore.helper.amplitude.shake

import com.meera.application_api.analytic.model.AmplitudeName
import com.meera.application_api.analytic.model.AmplitudeProperty
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyNameConst

enum class AmplitudeShakeEventName(
    private val event: String
) : AmplitudeName {

    SHAKE_RESULTS("shake results"),
    SHAKE_TOGGLE("shake toggle"),
    SHAKE_TAP("shake tap");

    override val eventName: String
        get() = event
}

enum class AmplitudeShakePositionProperty(
    private val property: String
) : AmplitudeProperty {
    ON("on"),
    OFF("off");

    override val _name: String
        get() = AmplitudePropertyNameConst.POSITION

    override val _value: String
        get() = property
}

enum class AmplitudeShakeHowProperty(
    private val property: String
) : AmplitudeProperty {
    BUTTON("button"),
    SHAKE("shake");

    override val _name: String
        get() = AmplitudePropertyNameConst.HOW

    override val _value: String
        get() = property
}

enum class AmplitudeShakeWhereProperty(
    private val property: String
) : AmplitudeProperty {
    PEOPLE("people"),
    SEARCH("search"),
    ACTION("action");

    override val _value: String
        get() = property
    override val _name: String
        get() = AmplitudePropertyNameConst.WHERE
}
