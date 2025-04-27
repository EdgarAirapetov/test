package com.numplates.nomera3.modules.volume.domain

import com.numplates.nomera3.modules.volume.data.VolumeStateRepository
import javax.inject.Inject

class SubscribeVolumeEventsUseCase @Inject constructor(
    private val volumeStateRepository: VolumeStateRepository
) {
    fun invoke() = volumeStateRepository.getEventStream()
}
