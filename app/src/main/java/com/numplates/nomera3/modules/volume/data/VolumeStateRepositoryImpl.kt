package com.numplates.nomera3.modules.volume.data

import com.meera.core.di.scopes.AppScope
import com.numplates.nomera3.modules.volume.domain.model.VolumeState
import com.numplates.nomera3.modules.volume.domain.model.VolumeRepositoryEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val VOLUME_EVENT_STREAM_BUFFER = 1

@AppScope
class VolumeStateRepositoryImpl @Inject constructor() : VolumeStateRepository {

    private var currentVolumeState = VolumeState.OFF

    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val eventStream = MutableSharedFlow<VolumeRepositoryEvent>(0, VOLUME_EVENT_STREAM_BUFFER)

    override fun getEventStream(): Flow<VolumeRepositoryEvent> {
        return eventStream
    }

    override fun setVolumeState(state: VolumeState) {
        coroutineScope.launch {
            currentVolumeState = state

            val event = VolumeRepositoryEvent.VolumeStateUpdated(state)
            eventStream.emit(event)
        }
    }

    override fun getVolumeState() = currentVolumeState
}
