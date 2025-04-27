package com.numplates.nomera3.modules.maps.domain.usecase

import com.numplates.nomera3.modules.feed.data.entity.PostEntityResponse
import com.numplates.nomera3.modules.maps.domain.repository.MapEventsRepository
import javax.inject.Inject

class GetEventPostUseCase @Inject constructor(private val repository: MapEventsRepository) {
    suspend fun invoke(postId: Long, refreshNewParticipants: Boolean = false): PostEntityResponse =
        repository.getEventPost(postId, refreshNewParticipants)
}
