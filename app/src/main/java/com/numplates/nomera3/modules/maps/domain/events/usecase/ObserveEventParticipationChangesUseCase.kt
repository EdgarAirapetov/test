package com.numplates.nomera3.modules.maps.domain.events.usecase

import com.numplates.nomera3.modules.feed.data.entity.PostEntityResponse
import com.numplates.nomera3.modules.maps.domain.repository.MapEventsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveEventParticipationChangesUseCase @Inject constructor(
    private val eventsRepository: MapEventsRepository
) {
    fun invoke(): Flow<PostEntityResponse> = eventsRepository.observeEventParticipationChanges()
}
