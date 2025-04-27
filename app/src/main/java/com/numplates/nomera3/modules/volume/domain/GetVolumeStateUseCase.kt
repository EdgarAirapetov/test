package com.numplates.nomera3.modules.volume.domain

import com.numplates.nomera3.modules.volume.data.VolumeStateRepository
import javax.inject.Inject

class GetVolumeStateUseCase @Inject constructor(
    private val volumeStateRepository: VolumeStateRepository
) {
    fun invoke() = volumeStateRepository.getVolumeState()
}
