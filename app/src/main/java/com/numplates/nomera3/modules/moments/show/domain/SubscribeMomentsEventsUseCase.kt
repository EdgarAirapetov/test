package com.numplates.nomera3.modules.moments.show.domain

import com.numplates.nomera3.modules.moments.show.data.MomentsRepository
import com.numplates.nomera3.modules.moments.show.domain.model.MomentRepositoryEvent
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SubscribeMomentsEventsUseCase @Inject constructor(
    private val momentsRepository: MomentsRepository
) {
    fun invoke(): Flow<MomentRepositoryEvent> {
        return momentsRepository.getEventStream()
    }
}
