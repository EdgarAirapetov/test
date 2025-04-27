package com.numplates.nomera3.modules.maps.domain.events.usecase

import com.numplates.nomera3.modules.feed.data.entity.PostEntityResponse
import com.numplates.nomera3.modules.maps.domain.events.model.GetMapEventSnippetsFullParamsModel
import com.numplates.nomera3.modules.maps.domain.events.model.GetMapEventSnippetsParamsModel
import com.numplates.nomera3.modules.maps.domain.repository.MapEventsRepository
import com.numplates.nomera3.modules.maps.domain.repository.MapSettingsRepository
import javax.inject.Inject

class GetMapEventSnippetsUseCase @Inject constructor(
    private val settingsRepository: MapSettingsRepository,
    private val eventsRepository: MapEventsRepository
) {
    suspend operator fun invoke(params: GetMapEventSnippetsParamsModel): List<PostEntityResponse> {
        val settings = settingsRepository.getMapSettings()
        val fullParams = GetMapEventSnippetsFullParamsModel(
            selectedEventId = params.selectedEventId,
            excludedEventIds = params.excludedEventIds,
            location = params.location,
            eventTypes = settings.eventFilters.eventTypeFilter,
            timeFilter = settings.eventFilters.eventDateFilter,
            limit = params.limit
        )
        return eventsRepository.getMapEventSnippets(fullParams)
    }
}
