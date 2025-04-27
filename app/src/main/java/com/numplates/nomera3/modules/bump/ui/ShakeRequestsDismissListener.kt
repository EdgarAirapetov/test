package com.numplates.nomera3.modules.bump.ui

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

interface ShakeRequestsDismissListener {

    fun emitShakeClosed()

    fun observeShakeRequestsClosed(): SharedFlow<Unit>
}

class ShakeRequestsDismissListenerImpl @Inject constructor() : ShakeRequestsDismissListener {

    private val shakeRequestsScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val shakeRequestsFlow = MutableSharedFlow<Unit>()

    override fun emitShakeClosed() {
        shakeRequestsScope.launch {
            shakeRequestsFlow.emit(Unit)
        }
    }

    override fun observeShakeRequestsClosed(): SharedFlow<Unit> {
        return shakeRequestsFlow.asSharedFlow()
    }
}
