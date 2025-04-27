package com.numplates.nomera3.data.websocket

import com.meera.core.network.websocket.WebSocketMainChannel
import javax.inject.Inject

class WebSocketConnectionStatusManager @Inject constructor(
    private val webSocket: WebSocketMainChannel
){
    fun isConnected(): Boolean  {
        return webSocket.isInitialized()
            && webSocket.isConnected()
            && webSocket.isChannelJoined()
    }
}
