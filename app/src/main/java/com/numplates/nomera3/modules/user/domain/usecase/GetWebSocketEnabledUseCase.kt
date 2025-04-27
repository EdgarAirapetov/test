package com.numplates.nomera3.modules.user.domain.usecase

import com.numplates.nomera3.data.websocket.WebSocketConnectionStatusManager
import javax.inject.Inject

class GetWebSocketEnabledUseCase @Inject constructor(
    private val webSocketConnectionStatusManager: WebSocketConnectionStatusManager
) {
    fun invoke() = webSocketConnectionStatusManager.isConnected()
}
