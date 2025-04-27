package com.numplates.nomera3.modules.calls.domain

import javax.inject.Inject

class RejectCallUsecase @Inject constructor(private val callManager: CallManager) {
    operator fun invoke(): Unit = callManager.rejectCall()
}
