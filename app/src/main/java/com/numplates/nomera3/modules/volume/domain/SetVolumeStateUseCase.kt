package com.numplates.nomera3.modules.volume.domain

import com.numplates.nomera3.modules.volume.domain.model.VolumeState
import com.numplates.nomera3.modules.volume.data.VolumeStateRepository
import javax.inject.Inject

class SetVolumeStateUseCase @Inject constructor(
    private val volumeStateRepository: VolumeStateRepository
) {
    fun invoke(volumeState: VolumeState) = volumeStateRepository.setVolumeState(volumeState)
}
