package com.numplates.nomera3.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.numplates.nomera3.modules.analytics.domain.AnalyticsInteractor
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyCallCanceller
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyCallType
import com.numplates.nomera3.modules.calls.domain.CallSignal
import com.numplates.nomera3.modules.calls.domain.GetCallStatesUsecase
import com.numplates.nomera3.modules.calls.domain.RejectCallUsecase
import com.numplates.nomera3.modules.calls.domain.StartCallUsecase
import com.numplates.nomera3.modules.calls.domain.StopCallUsecase
import com.numplates.nomera3.modules.calls.presentation.CallViewEvent
import com.numplates.nomera3.presentation.utils.networkconn.NetworkStatusProvider
import com.numplates.nomera3.telecom.MeeraSignalingServiceConnectionWrapper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class CallViewModel @Inject constructor(
    private val tracker: AnalyticsInteractor,
    private val networkStatusProvider: NetworkStatusProvider,
    private val getCallStatesUsecase: GetCallStatesUsecase,
    private val startCallUsecase: StartCallUsecase,
    private val stopCallUsecase: StopCallUsecase,
    private val rejectCallUsecase: RejectCallUsecase,
    private val meeraSignalingServiceConnectionWrapper: MeeraSignalingServiceConnectionWrapper
) : BaseViewModel() {

    private val _viewEventFlow = MutableSharedFlow<CallViewEvent>()
    val viewEventFlow: Flow<CallViewEvent> = _viewEventFlow

    init {
        getCallStatesUsecase.invoke().also { collectCallSignals(it) }
    }

    fun collectCallSignals(callSignalFlow: Flow<CallSignal>) {
        callSignalFlow.onEach { callSignal ->
            when (callSignal) {
                CallSignal.INITIATE_CALL -> emitEvent(CallViewEvent.InitiateCall)
                CallSignal.ACCEPT_CALL -> emitEvent(CallViewEvent.AcceptCall)
                CallSignal.REJECT_CALL -> {
                    rejectCallUsecase.invoke()
                    emitEvent(CallViewEvent.RejectCall)
                }

                CallSignal.STOP_CALL -> emitEvent(CallViewEvent.StopCall)
                CallSignal.LINE_BUSY -> emitEvent(CallViewEvent.LineBusy)
                CallSignal.OFFER -> emitEvent(CallViewEvent.Offer)
                CallSignal.ANSWER -> emitEvent(CallViewEvent.Answer)
                CallSignal.CANDIDATES -> emitEvent(CallViewEvent.Candidates)
                CallSignal.CANDIDATES_REMOVE -> emitEvent(CallViewEvent.CandidatesRemove)
                CallSignal.GET_ICE -> emitEvent(CallViewEvent.GetIceServers)
            }
        }.launchIn(viewModelScope)
    }

    fun logCallCancel(callCanceller: AmplitudePropertyCallCanceller) {
        rejectCallUsecase.invoke()
        tracker.logCallCancel(callCanceller)
    }

    fun logCall(callStartedTimeMs: Long, isIncomingCall: Boolean, videoEnabled: Boolean) {
        val callDuration = System.currentTimeMillis() - callStartedTimeMs
        val hours = TimeUnit.MILLISECONDS.toHours(callDuration)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(callDuration) - TimeUnit.HOURS.toMinutes(hours)
        val seconds = TimeUnit.MILLISECONDS.toSeconds(callDuration) -
            (TimeUnit.HOURS.toSeconds(hours) + TimeUnit.MINUTES.toSeconds(minutes))
        val callDurationText = String.format("%02d:%02d:%02d", hours, minutes, seconds)

        val callType = if (isIncomingCall) AmplitudePropertyCallType.INCOMING else AmplitudePropertyCallType.OUTGOING

        tracker.logCall(videoEnabled, callDurationText, callType)
    }

    fun isInternetConnected(): Boolean {
        return networkStatusProvider.isInternetConnected()
    }

    /**
     * This method needs to be called when call started as params used id and roomID
     * */
    fun onCallStarted(id: String, roomId: Long) = startCallUsecase.invoke(roomId = roomId, uid = id)

    /**
     * This method needs to be called when call finished as params used id and roomID
     * */
    fun onCallFinished(id: String, roomId: Long) = stopCallUsecase.invoke(roomId = roomId, uid = id)

    fun getSignalingServiceConnectionWrapper() = meeraSignalingServiceConnectionWrapper

    private fun emitEvent(event: CallViewEvent) = viewModelScope.launch { _viewEventFlow.emit(event) }
}
