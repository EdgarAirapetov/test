package com.numplates.nomera3.modules.volume.domain.model

sealed class VolumeRepositoryEvent {
    data class VolumeStateUpdated(
        val volumeState: VolumeState
    ) : VolumeRepositoryEvent()
}
