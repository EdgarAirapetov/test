package com.numplates.nomera3.modules.volume.presentation

import com.numplates.nomera3.modules.volume.domain.model.VolumeState

interface VolumeStateCallback {
    fun setVolumeState(volumeState: VolumeState)

    fun getVolumeState(): VolumeState
}
