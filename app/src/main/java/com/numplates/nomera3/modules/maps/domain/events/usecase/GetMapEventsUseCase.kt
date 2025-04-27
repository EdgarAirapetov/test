package com.numplates.nomera3.modules.maps.domain.events.usecase

import com.numplates.nomera3.modules.feed.data.entity.PostEntityResponse
import com.numplates.nomera3.modules.maps.domain.events.model.GetMapEventsParamsModel
import com.numplates.nomera3.modules.maps.domain.model.MapBoundsModel
import com.numplates.nomera3.modules.maps.domain.repository.MapEventsRepository
import com.numplates.nomera3.modules.maps.domain.repository.MapSettingsRepository
import javax.inject.Inject

class GetMapEventsUseCase @Inject constructor(
    private val settingsRepository: MapSettingsRepository,
    private val eventsRepository: MapEventsRepository
) {
    suspend operator fun invoke(bounds: MapBoundsModel): List<PostEntityResponse> {
        val settings = settingsRepository.getMapSettings()
        val params = GetMapEventsParamsModel(
            bounds = bounds,
            eventTypes = settings.eventFilters.eventTypeFilter,
            timeFilter = settings.eventFilters.eventDateFilter
        )
        return eventsRepository.getMapEvents(params)
    }
}
