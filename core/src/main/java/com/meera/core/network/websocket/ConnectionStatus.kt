package com.meera.core.network.websocket

sealed class ConnectionStatus {

    data class OnSocketOpened(val isOpened: Boolean) : ConnectionStatus()

    data class OnChannelJoined(val isJoined: Boolean) : ConnectionStatus()

    object SocketDisconnected: ConnectionStatus()
}
