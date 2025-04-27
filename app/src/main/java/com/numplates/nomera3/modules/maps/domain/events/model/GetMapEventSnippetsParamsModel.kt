package com.numplates.nomera3.modules.maps.domain.events.model

import com.numplates.nomera3.modules.baseCore.domain.model.CoordinatesModel

data class GetMapEventSnippetsParamsModel(
    val selectedEventId: Long,
    val excludedEventIds: List<Long>,
    val location: CoordinatesModel,
    val limit: Int
)
