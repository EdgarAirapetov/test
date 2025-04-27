package com.numplates.nomera3.telecom

enum class MessageType(val type: String) {
    // call progress
    INITIATE_CALL("start_call"),
    ACCEPT_CALL("accept_call"),
    REJECT_CALL("reject_call"),
    STOP_CALL("stop_call"),
    LINE_BUSY("line_busy"),

    // webrtc
    OFFER("offer"),
    ANSWER("answer"),
    CANDIDATES("candidates"),
    CANDIDATES_REMOVE("candidates_remove"),
    GET_ICE("get_ice") // stun/turns servers
}