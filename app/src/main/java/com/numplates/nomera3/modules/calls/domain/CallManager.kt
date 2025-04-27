package com.numplates.nomera3.modules.calls.domain

import kotlinx.coroutines.flow.Flow

interface CallManager {
    fun getCallStateFlow(): Flow<CallSignal>
    fun startCall(roomId: Long, uid: String)
    fun stopCall(roomId: Long, uid: String)
    fun rejectCall()
    fun getCallStatusFlow(): Flow<Unit>
}
