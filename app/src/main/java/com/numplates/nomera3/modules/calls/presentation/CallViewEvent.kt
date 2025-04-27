package com.numplates.nomera3.modules.calls.presentation

sealed class CallViewEvent {
    object InitiateCall: CallViewEvent()

    object AcceptCall: CallViewEvent()

    object RejectCall: CallViewEvent()

    object StopCall: CallViewEvent()

    object LineBusy: CallViewEvent()

    object Offer: CallViewEvent()

    object Answer: CallViewEvent()

    object Candidates: CallViewEvent()

    object CandidatesRemove: CallViewEvent()

    object GetIceServers: CallViewEvent()
}
