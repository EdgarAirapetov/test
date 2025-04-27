package com.numplates.nomera3.modules.calls.domain

import javax.inject.Inject

class StartCallUsecase @Inject constructor(private val callManager: CallManager) {
    operator fun invoke(roomId: Long, uid: String): Unit = callManager.startCall(roomId = roomId, uid = uid)
}
