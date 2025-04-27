package com.numplates.nomera3.modules.calls.domain

enum class CallSignal {
    INITIATE_CALL,
    ACCEPT_CALL,
    REJECT_CALL,
    STOP_CALL,
    LINE_BUSY,
    OFFER,
    ANSWER,
    CANDIDATES,
    CANDIDATES_REMOVE,
    GET_ICE;
}
