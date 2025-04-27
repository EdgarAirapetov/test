package com.numplates.nomera3.modules.maps.domain.events.usecase

import com.numplates.nomera3.modules.maps.domain.repository.MapEventsRepository
import javax.inject.Inject

class NeedToShowEventsOnboardingUseCase @Inject constructor(
    private val repository: MapEventsRepository
) {
    operator fun invoke(): Boolean = repository.needToShowEventsOnboarding()
}
