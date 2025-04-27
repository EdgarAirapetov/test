package com.numplates.nomera3.telecom

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.meera.core.network.websocket.WebSocketMainChannel
import com.meera.core.preferences.AppSettings
import io.reactivex.disposables.CompositeDisposable
import timber.log.Timber
import javax.inject.Inject

private const val TAG = "CallWebSocketManager"

class CallWebSocketManager @Inject constructor(
    private val webSocketChannel: WebSocketMainChannel,
    private val appSettings: AppSettings,
) : DefaultLifecycleObserver {

    private val disposables by lazy { CompositeDisposable() }

    override fun onDestroy(owner: LifecycleOwner) {
        disposables.dispose()
        disposables.clear()
        webSocketChannel.disconnectAll()
    }

    @Throws(IllegalStateException::class)
    fun prepareSocketConnection(onReadyToUse: () -> Unit, onError: (Throwable) -> Unit) {
        if (!webSocketChannel.isInitialized()) {
            webSocketChannel.initSocket(getUserAccessToken())
            Timber.tag(TAG).d("Channel is not installed. Installing...")
        }
        if (!webSocketChannel.isChannelJoined() || !webSocketChannel.isConnected()) {
            Timber.tag(TAG).d("Channel is not connected | joined. Setting up channel...")
            webSocketChannel.rejoinChannel(
                onSuccess = {
                    Timber.tag(TAG).d("Channel has been connected | joined.")
                    onReadyToUse.invoke()
                },
                onError = {
                    Timber.tag(TAG).e("Can not setup channel correctly. Reason: ${it};")
                    onError.invoke(Exception(it))
                },
            )
            return
        }
        if (webSocketChannel.isInitialized() &&
            webSocketChannel.isConnected() &&
            webSocketChannel.isChannelJoined()
        ) {
            onReadyToUse.invoke()
        } else {
            error("Socket connection should be initialized at this step. Please check code.")
        }
    }

    @Throws(IllegalStateException::class)
    private fun getUserAccessToken(): String {
        val token = appSettings.readAccessToken()
        if (!token.isNullOrBlank()) {
            Timber.tag(TAG).d("Token was read successfully. accessToken: ${token};")
            return token
        } else {
            error("User's token can not be blank. Check that system is consistent.")
        }
    }
}
