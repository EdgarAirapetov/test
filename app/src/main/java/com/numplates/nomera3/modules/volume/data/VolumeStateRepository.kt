package com.numplates.nomera3.modules.volume.data

import com.numplates.nomera3.modules.volume.domain.model.VolumeState
import com.numplates.nomera3.modules.volume.domain.model.VolumeRepositoryEvent
import kotlinx.coroutines.flow.Flow

interface VolumeStateRepository {
    fun getEventStream(): Flow<VolumeRepositoryEvent>

    fun setVolumeState(state: VolumeState)

    fun getVolumeState(): VolumeState
}
