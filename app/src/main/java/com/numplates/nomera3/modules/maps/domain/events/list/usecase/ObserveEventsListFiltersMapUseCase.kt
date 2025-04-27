package com.numplates.nomera3.modules.maps.domain.events.list.usecase

import com.numplates.nomera3.modules.maps.domain.events.list.model.EventsListFiltersModel
import com.numplates.nomera3.modules.maps.domain.events.model.EventsListType
import com.numplates.nomera3.modules.maps.domain.repository.MapEventsListsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveEventsListFiltersMapUseCase @Inject constructor(
    private val mapEventsListsRepository: MapEventsListsRepository
) {
    fun invoke(): Flow<Map<EventsListType, EventsListFiltersModel>> = mapEventsListsRepository.observeEventsListsFilters()
}
