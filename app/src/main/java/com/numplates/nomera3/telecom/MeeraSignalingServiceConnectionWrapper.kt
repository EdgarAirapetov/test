package com.numplates.nomera3.telecom

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import com.meera.core.di.scopes.AppScope
import com.meera.core.network.websocket.WebSocketMainChannel
import com.meera.db.models.dialog.UserChat
import com.numplates.nomera3.CALL_ACTION_ACCEPT_CALL
import com.numplates.nomera3.TYPE_CALL_ACTION
import com.numplates.nomera3.presentation.router.IArgContainer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import timber.log.Timber
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

private const val SERVICE_CONNECTION_TIMEOUT_MS = 30_000L

@AppScope
class MeeraSignalingServiceConnectionWrapper @Inject constructor(
    private val webSocketChannel: WebSocketMainChannel
) {

    /**
     * Underlying signaling service, through it you can
     * directly take action concerning the current call
     */
    var signalingService: MeeraSignalingService? = null
        private set

    /**
     * Whether there's a call/CallFragment present at this moment
     */
    var isCallActive: Boolean = false

    /**
     * Whether there's both an internet and service connection present
     * Can be safely removed when logic for the Internet Connection Toast is updated
     * and not tied to socket/service connection
     */
    var connectionEstablished = AtomicBoolean(false)

    /**
     * Callback to be triggered when a call is initiated
     */
    var onStartCall: ((Boolean, UserChat?, Boolean?) -> Unit)? = null

    private var isServiceConnected = AtomicBoolean(false)
    private var callUser: UserChat? = null
    private var callAccepted: Boolean? = null
    private var callRoomId: Long? = null
    private var callMessageId: String? = null

    private val signalingServiceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceDisconnected(componentName: ComponentName) {
            signalingService = null
            Timber.d("MAIN_ACT_VM SignalingService was disconnected.")
        }

        override fun onServiceConnected(componentName: ComponentName, service: IBinder) {
            val binder = service as MeeraSignalingServiceBinder
            signalingService = binder.getService()?.get()
            isServiceConnected.set(true)
            Timber.e("MAIN_ACT_VM START_CALL_TEST: on MeeraCallService connected:$signalingService")
            signalingService?.onStartCall = { isIncoming, callUser, callAccepted ->
                Timber.e("MAIN_ACT_VM START_CALL_TEST: $callUser $isIncoming $callAccepted")
                onStartCall?.invoke(isIncoming, callUser, callAccepted)
                this@MeeraSignalingServiceConnectionWrapper.callUser = null
                this@MeeraSignalingServiceConnectionWrapper.callAccepted = null
                this@MeeraSignalingServiceConnectionWrapper.callRoomId = null
                this@MeeraSignalingServiceConnectionWrapper.callMessageId = null
            }
        }
    }

    fun bindService(act: Activity) {
        if (!isServiceConnected.get()) Timber.d("MAIN_ACT_VM SignalingService status: bind service")
        act.bindService(
            Intent(act, MeeraSignalingService::class.java),
            signalingServiceConnection,
            Context.BIND_AUTO_CREATE
        )
    }

    fun unbindService(act: Activity) {
        if (isServiceConnected.get()) {
            Timber.d("MAIN_ACT_VM SignalingService status: unbind service")
            act.unbindService(signalingServiceConnection)
        }
        isServiceConnected.set(false)
    }

    fun placeWebRtcCall(
        callUser: UserChat?,
        isIncoming: Boolean,
        callAccepted: Boolean?,
        roomId: Long?,
        messageId: String?
    ) {
        val service = signalingService ?: return
        val caller = callUser ?: return
        isCallActive = true
        Timber.e("MAIN_ACT_VM wrapper place webrtc call")
        service.cmdPlaceCall(caller, isIncoming, callAccepted, roomId, messageId)
    }

    fun actionStartCall(
        extras: Bundle?,
        onCallInfoNotNull: () -> Unit
    ) {
        signalingService?.cancelCallNotification()
        callUser = extras?.getParcelable(IArgContainer.ARG_USER_MODEL)
        callAccepted = extras?.getInt(TYPE_CALL_ACTION) == CALL_ACTION_ACCEPT_CALL
        callRoomId = extras?.getLong(IArgContainer.ARG_ROOM_ID)
        callMessageId = extras?.getString(IArgContainer.ARG_MESSAGE_ID)
        ifNotNull(callUser, callRoomId, callMessageId, callAccepted) {
            if (webSocketChannel.isConnected()) {
                placeWebRtcCall(callUser, true, callAccepted, callRoomId, callMessageId)
            }
            onCallInfoNotNull.invoke()
        }
    }

    suspend fun checkServiceConnection(onServiceConnected: suspend () -> Unit) {
        withTimeoutOrNull(SERVICE_CONNECTION_TIMEOUT_MS) {
            var checking = true
            while (checking) {
                delay(100)
                if (isServiceConnected.get()) {
                    checking = false
                }
            }
        } ?: kotlin.run {
            Timber.e("Checking service connection timed out after ${SERVICE_CONNECTION_TIMEOUT_MS/1_000}s")
            return
        }
        onServiceConnected.invoke()
        withContext(Dispatchers.Main) {
            connectionEstablished.set(true)
            signalingService?.observeSignalingChannel {
                ifNotNull(callAccepted, callMessageId, callRoomId, callUser) {
                    if (!isCallActive) {
                        placeWebRtcCall(
                            callUser,
                            true,
                            callAccepted,
                            callRoomId,
                            callMessageId
                        )
                    }
                }
            }
        }
    }

    private inline fun ifNotNull(vararg values: Any?, crossinline block: () -> Unit): Unit? {
        values.forEach {
            if (it == null) return null
        }
        return block.invoke()
    }


}
