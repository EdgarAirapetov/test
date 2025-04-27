package com.numplates.nomera3.modules.baseCore.helper.amplitude.map

import com.meera.application_api.analytic.model.AmplitudeName

enum class AmplitudeMapEventName(
    private val event: String
) : AmplitudeName {
    FIND_ME("find me"),
    MAP_OPEN("map open");

    override val eventName: String
        get() = event
}
