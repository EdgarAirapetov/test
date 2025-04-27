package com.numplates.nomera3.modules.maps.domain.events.list.usecase

import com.numplates.nomera3.modules.maps.domain.events.list.model.EventsListFiltersModel
import com.numplates.nomera3.modules.maps.domain.repository.MapEventsListsRepository
import javax.inject.Inject

class SetEventsListFiltersUseCase @Inject constructor(private val mapEventsRepository: MapEventsListsRepository) {
    fun invoke(filters: EventsListFiltersModel) = mapEventsRepository.setEventsListFilters(filters)
}
