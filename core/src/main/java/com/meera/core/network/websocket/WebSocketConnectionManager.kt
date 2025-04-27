package com.meera.core.network.websocket

import timber.log.Timber
import javax.inject.Inject

/**
 * todo https://nomera.atlassian.net/browse/BR-30131
 */
class WebSocketConnectionManager @Inject constructor(
    // private val socket: WebSocketMainChannel
) {

    fun test() {
        Timber.d("SOCKET_LOG Socket connect manager TEST()!!!!!")
    }

}
