package com.numplates.nomera3.modules.maps.domain.events.usecase

import com.numplates.nomera3.modules.maps.domain.repository.MapEventsRepository
import javax.inject.Inject

class RemoveEventParticipantUseCase @Inject constructor(
    private val eventsRepository: MapEventsRepository
) {
    suspend fun invoke(eventId: Long, userId: Long) {
        eventsRepository.removeEventParticipant(eventId = eventId, userId = userId)
    }
}
