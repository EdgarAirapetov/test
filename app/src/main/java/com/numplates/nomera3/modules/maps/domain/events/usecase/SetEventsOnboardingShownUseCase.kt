package com.numplates.nomera3.modules.maps.domain.events.usecase

import com.numplates.nomera3.modules.maps.domain.repository.MapEventsRepository
import javax.inject.Inject

class SetEventsOnboardingShownUseCase @Inject constructor(
    private val repository: MapEventsRepository
) {
    operator fun invoke() = repository.setEventsOnboardingShown()
}
