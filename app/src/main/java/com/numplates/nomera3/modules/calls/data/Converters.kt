package com.numplates.nomera3.modules.calls.data

import com.numplates.nomera3.modules.calls.domain.CallSignal
import com.numplates.nomera3.telecom.MessageType
import com.numplates.nomera3.telecom.SignalingMsgPayload

fun SignalingMsgPayload.getCallSignal(): CallSignal {
    return when (this.type) {
        MessageType.INITIATE_CALL.type -> CallSignal.INITIATE_CALL
        MessageType.ACCEPT_CALL.type -> CallSignal.ACCEPT_CALL
        MessageType.REJECT_CALL.type -> CallSignal.REJECT_CALL
        MessageType.STOP_CALL.type -> CallSignal.STOP_CALL
        MessageType.LINE_BUSY.type -> CallSignal.LINE_BUSY
        MessageType.OFFER.type -> CallSignal.OFFER
        MessageType.ANSWER.type -> CallSignal.ANSWER
        MessageType.CANDIDATES.type -> CallSignal.CANDIDATES
        MessageType.CANDIDATES_REMOVE.type -> CallSignal.CANDIDATES_REMOVE
        MessageType.GET_ICE.type -> CallSignal.GET_ICE
        else -> error("Did not expect signal type \"${this.type}\"")
    }
}
