package com.meera.core.network.websocket

class WebSocketResponseException(override val message: String?) : Exception() {

    private lateinit var payloadInternal: Map<String, Any?>

    constructor(payload: Map<String, Any?>) : this("") {
        this.payloadInternal = payload
    }

    fun isValid():Boolean {
        return ::payloadInternal.isInitialized
    }

    fun getPayload(): Map<String, Any?> {
        return payloadInternal
    }
}
