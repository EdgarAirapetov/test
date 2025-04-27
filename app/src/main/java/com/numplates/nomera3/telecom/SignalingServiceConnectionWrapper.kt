package com.numplates.nomera3.telecom

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import com.meera.core.network.websocket.WebSocketMainChannel
import com.meera.db.models.dialog.UserChat
import com.numplates.nomera3.CALL_ACTION_ACCEPT_CALL
import com.numplates.nomera3.TYPE_CALL_ACTION
import com.numplates.nomera3.presentation.router.IArgContainer
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_MESSAGE_ID
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_ROOM_ID
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_USER_MODEL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import timber.log.Timber
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

private const val SERVICE_CONNECTION_TIMEOUT_MS = 30_000L

class SignalingServiceConnectionWrapper @Inject constructor(
    private val webSocketChannel: WebSocketMainChannel
) {

    /**
     * Underlying signaling service, through it you can
     * directly take action concerning the current call
     */
    var signalingService: SignalingService? = null
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
            Timber.d("SignalingService was disconnected.")
        }

        override fun onServiceConnected(componentName: ComponentName, service: IBinder) {
            val binder = service as SignalingServiceBinder
            signalingService = binder.getService()?.get()
            isServiceConnected.set(true)
            signalingService?.onStartCall = { isIncoming, callUser, callAccepted ->
                Timber.e("START_CALL_TEST: $callUser $isIncoming $callAccepted")
                onStartCall?.invoke(isIncoming, callUser, callAccepted)
                this@SignalingServiceConnectionWrapper.callUser = null
                this@SignalingServiceConnectionWrapper.callAccepted = null
                this@SignalingServiceConnectionWrapper.callRoomId = null
                this@SignalingServiceConnectionWrapper.callMessageId = null
            }
        }
    }

    fun bindService(act: Activity) {
        if (!isServiceConnected.get()) Timber.d("CALL_LOG WrapperBindService SignalingService status: bind service")
        val intent = Intent(act, SignalingService::class.java)
        intent.putExtra(ARG_USER_MODEL, callUser)
        intent.putExtra(TYPE_CALL_ACTION, callAccepted)
        intent.putExtra(ARG_ROOM_ID, callRoomId)
        intent.putExtra(ARG_MESSAGE_ID, callMessageId)
        act.bindService(
            intent,
            signalingServiceConnection,
            Context.BIND_AUTO_CREATE
        )
    }

    fun unbindService(act: Activity) {
        if (isServiceConnected.get()) {
            Timber.d("SignalingService status: unbind service")
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
        service.cmdPlaceCall(caller, isIncoming, callAccepted, roomId, messageId)
    }

    fun actionStartCall(
        extras: Bundle?,
        onCallInfoNotNull: () -> Unit
    ) {
        signalingService?.cancelCallNotification()
        callUser = extras?.getParcelable(ARG_USER_MODEL)
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
