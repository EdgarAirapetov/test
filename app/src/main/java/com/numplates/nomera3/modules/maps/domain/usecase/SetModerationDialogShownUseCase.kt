package com.numplates.nomera3.modules.maps.domain.usecase

import com.numplates.nomera3.modules.maps.domain.repository.MapEventsRepository
import javax.inject.Inject

class SetModerationDialogShownUseCase @Inject constructor(
    private val repository: MapEventsRepository
) {
    fun invoke() = repository.setEventModerationDialogShown()
}
