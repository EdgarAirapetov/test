package com.numplates.nomera3.modules.maps.domain.events.usecase

import com.numplates.nomera3.modules.maps.domain.events.model.EventModel
import com.numplates.nomera3.modules.maps.domain.repository.MapEventsRepository
import javax.inject.Inject

class GetEventUseCase @Inject constructor(
    private val eventsRepository: MapEventsRepository
) {
    suspend fun invoke(postId: Long): EventModel = eventsRepository.getEvent(postId)
}
