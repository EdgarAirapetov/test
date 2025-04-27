package com.numplates.nomera3.modules.maps.domain.events.usecase

import com.numplates.nomera3.modules.maps.domain.events.model.EventFiltersModel
import com.numplates.nomera3.modules.maps.domain.events.model.FilterEventDate
import javax.inject.Inject

class GetDefaultEventFiltersUseCase @Inject constructor() {
    operator fun invoke(): EventFiltersModel = EventFiltersModel(
        eventTypeFilter = listOf(),
        eventDateFilter = FilterEventDate.ALL
    )
}
