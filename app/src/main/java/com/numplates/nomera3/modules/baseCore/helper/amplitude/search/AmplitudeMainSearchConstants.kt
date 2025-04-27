package com.numplates.nomera3.modules.baseCore.helper.amplitude.search

import com.meera.application_api.analytic.model.AmplitudeName
import com.meera.application_api.analytic.model.AmplitudeProperty
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyNameConst

enum class AmplitudeMainSearchEventName(
    private val event: String
) : AmplitudeName {
    SEARCH_OPEN("search open");

    override val eventName: String
        get() = event
}

enum class AmplitudeSearchOpenWhereProperty(
    private val property: String
) : AmplitudeProperty {

    SELF_FEED("self feed"),
    MAIN_FEED("main feed"),
    FOLLOW_FEED("follow feed"),
    PEOPLE("people");

    override val _name: String
        get() = AmplitudePropertyNameConst.WHERE

    override val _value: String
        get() = property
}
