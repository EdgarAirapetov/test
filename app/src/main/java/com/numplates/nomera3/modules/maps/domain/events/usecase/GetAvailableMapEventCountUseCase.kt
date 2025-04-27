package com.numplates.nomera3.modules.maps.domain.events.usecase

import com.numplates.nomera3.modules.maps.domain.events.EventConstants
import com.numplates.nomera3.modules.maps.domain.repository.MapEventsRepository
import javax.inject.Inject

class GetAvailableMapEventCountUseCase @Inject constructor(
    private val repository: MapEventsRepository
) {
    suspend operator fun invoke(): Int = EventConstants.MAX_USER_EVENT_COUNT - repository.getActiveEventCount()
}
