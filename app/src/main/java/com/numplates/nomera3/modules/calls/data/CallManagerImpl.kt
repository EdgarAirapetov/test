package com.numplates.nomera3.modules.calls.data

import com.google.gson.Gson
import com.meera.core.di.scopes.AppScope
import com.meera.core.extensions.fromJson
import com.meera.core.network.websocket.WebSocketMainChannel
import com.numplates.nomera3.modules.calls.domain.CallManager
import com.numplates.nomera3.modules.calls.domain.CallSignal
import com.numplates.nomera3.telecom.SignalingMsgPayload
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject

@AppScope
class CallManagerImpl @Inject constructor(
    private val websocketChannel: WebSocketMainChannel,
    private val gson: Gson
) : CallManager {

    private val callStatusFlow = MutableSharedFlow<Unit>(replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    override fun getCallStateFlow(): Flow<CallSignal> {
        return websocketChannel.observeSignallingFlow()
            .map { gson.fromJson<SignalingMsgPayload>(gson.toJson(it.payload)) }
            .map { it.getCallSignal() }
    }

    override fun startCall(roomId: Long, uid: String) {
        mapOf(
            "id" to uid,
            "room_id" to roomId
        ).let {
            callStatusFlow.tryEmit(Unit)
            websocketChannel.pushCallStarted(it)
        }
    }

    override fun stopCall(roomId: Long, uid: String) {
        mapOf(
            "id" to uid,
            "room_id" to roomId
        ).let {
            Timber.d("MAP SC stop call")
            callStatusFlow.tryEmit(Unit)
            websocketChannel.pushCallFinished(it)
        }
    }

    override fun rejectCall() {
        val tryEmit = callStatusFlow.tryEmit(Unit)
    }

    override fun getCallStatusFlow(): Flow<Unit> = callStatusFlow
}
