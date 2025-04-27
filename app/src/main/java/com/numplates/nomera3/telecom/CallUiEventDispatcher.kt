package com.numplates.nomera3.telecom

import com.meera.core.di.scopes.AppScope
import com.numplates.nomera3.telecom.model.CallUiEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@AppScope
class CallUiEventDispatcher @Inject constructor() {

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val eventsSharedFlow = MutableSharedFlow<CallUiEvent>()

    fun setEvent(event: CallUiEvent) {
        scope.launch {
            eventsSharedFlow.emit(event)
        }
    }

    fun eventFlow(): Flow<CallUiEvent> = eventsSharedFlow
}
