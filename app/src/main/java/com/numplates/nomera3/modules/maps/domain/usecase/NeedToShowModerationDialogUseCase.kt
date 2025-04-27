package com.numplates.nomera3.modules.maps.domain.usecase

import com.numplates.nomera3.modules.maps.domain.repository.MapEventsRepository
import javax.inject.Inject

class NeedToShowModerationDialogUseCase @Inject constructor(
    private val repository: MapEventsRepository
) {
    fun invoke(): Boolean = repository.needToShowEventModerationDialog()
}
