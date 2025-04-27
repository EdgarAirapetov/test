package com.numplates.nomera3.modules.calls.domain

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCallStatesUsecase @Inject constructor(private val callManager: CallManager) {
    operator fun invoke(): Flow<CallSignal> = callManager.getCallStateFlow()
}
