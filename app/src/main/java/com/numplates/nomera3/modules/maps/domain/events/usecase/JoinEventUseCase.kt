package com.numplates.nomera3.modules.maps.domain.events.usecase

import com.numplates.nomera3.modules.feed.data.entity.PostEntityResponse
import com.numplates.nomera3.modules.maps.domain.repository.MapEventsRepository
import javax.inject.Inject

class JoinEventUseCase @Inject constructor(
    private val eventsRepository: MapEventsRepository
) {
    suspend fun invoke(eventId: Long): PostEntityResponse = eventsRepository.joinEvent(eventId)
}
