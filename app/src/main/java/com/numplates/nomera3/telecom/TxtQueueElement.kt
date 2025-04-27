package com.numplates.nomera3.telecom

class TxtQueueElement(method: String, payload: Map<String, Any>) {
    val m: String = method
    val p: Map<String, Any> = payload
}