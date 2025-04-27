package com.numplates.nomera3.modules.maps.domain.events.usecase

import com.numplates.nomera3.modules.maps.domain.events.EventConstants
import com.numplates.nomera3.modules.userprofile.data.repository.ProfileRepository
import javax.inject.Inject

class GetAvailableMapEventCountFromLocalProfileUseCase @Inject constructor(
    private val repository: ProfileRepository
) {
    suspend operator fun invoke(): Int? = repository.getOwnLocalProfile()
        ?.eventCount
        ?.let { eventCount -> EventConstants.MAX_USER_EVENT_COUNT - eventCount }
}
