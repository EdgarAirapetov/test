package com.numplates.nomera3.telecom


import android.content.Context
import android.os.Bundle
import android.os.PowerManager
import androidx.activity.ComponentActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.meera.core.extensions.safeNavigate
import com.meera.core.network.websocket.WebSocketMainChannel
import com.meera.db.models.dialog.UserChat
import com.numplates.nomera3.INCOMING_CALL_KEY
import com.numplates.nomera3.OUTGOING_CALL_KEY
import com.numplates.nomera3.R
import com.numplates.nomera3.TYPE_CALL_KEYS
import com.numplates.nomera3.modules.redesign.util.NavigationManager
import com.numplates.nomera3.presentation.router.IArgContainer
import kotlinx.coroutines.launch
import timber.log.Timber


private const val WAKELOCK_TAG = "NUMAD:CallWakelock"

// TODO: В данный момент звонки не могут работать и в режиме пуша (не открытое проиложение) и при открытом приложении
private const val IS_CALL_FROM_PUSH_MODE = false

class MeeraCallDelegate(
    private val activity: ComponentActivity,
    private val signalingServiceConnectionWrapper: MeeraSignalingServiceConnectionWrapper,
    private val socket: WebSocketMainChannel
) : DefaultLifecycleObserver {

    interface OnActivityCallInteraction {
        fun onStartCall(
            user: UserChat,
            isIncoming: Boolean,
            callAccepted: Boolean?,
            roomId: Long?,
            messageId: String?
        )
    }

    private var wakeLock: PowerManager.WakeLock? = null

    init {
        activity.lifecycle.addObserver(this)
    }

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        if (IS_CALL_FROM_PUSH_MODE) {
            initForIncomingPushCall()
        } else {
            initForOutgoingCall()
        }
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        signalingServiceConnectionWrapper.isCallActive = false
        unbindService()
        shouldShowOnLockScreen(false)
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        unbindService()
    }

    fun placeWebRtcCall(
        callUser: UserChat,
        isIncoming: Boolean,
        callAccepted: Boolean?,
        roomId: Long?,
        messageId: String?
    ) {
        signalingServiceConnectionWrapper.placeWebRtcCall(callUser, isIncoming, callAccepted, roomId, messageId)
    }

    fun handleCallFragmentActions(actions: MeeraCallFragment.CallFragmentActions) {
        when (actions) {
            is MeeraCallFragment.CallFragmentActions.OnCallFinished -> callFinished()
            is MeeraCallFragment.CallFragmentActions.DismissAllDialogs -> {
                Timber.d("Dismiss all Dialogs from Act")
                // TODO: LEGACY LOGIC
            }
        }
    }

    private fun callFinished() {
        // TODO callUiEventDispatcher.setEvent(CallUiEvent.DESTROYED)
        shouldShowOnLockScreen(false)
        onGetCallFragment { _, _ ->
            NavigationManager.getManager().topNavController.popBackStack()
        }
    }

    /**
     * Звонки запускаемые когда приложение вышружено и мы
     * кликаем по пушу
     */
    private fun initForIncomingPushCall() {
        onSocketInitialized {
            socketStateListenerForIncomingCall()
            initStartCallObserver()
            bindService()
        }
    }

    /**
     * Инициализация звонков если приложение не выгружено
     */
    private fun initForOutgoingCall() {
        onSocketInitialized {
            socketStateListenerForOutgoingCall()
            initStartCallObserver()
            bindService()
        }
    }


    private fun initStartCallObserver() {
        signalingServiceConnectionWrapper.onStartCall = { isIncoming, callUser, callAccepted ->
            startCall(callUser, isIncoming, callAccepted)
        }
    }

    private fun startCall(callUser: UserChat?, isIncoming: Boolean, callAccepted: Boolean?) {
        onGetCallFragment { fragment, fragmentManager ->
            if (fragment == null && !callUser?.name.isNullOrBlank()) {
                // TODO callUiEventDispatcher.setEvent(CallUiEvent.CREATED)
                val isIncome = if (isIncoming) INCOMING_CALL_KEY else OUTGOING_CALL_KEY
                val bundle = Bundle().apply {
                    putInt(TYPE_CALL_KEYS, isIncome)
                    putParcelable(IArgContainer.ARG_USER_MODEL, callUser)
                    putBoolean(IArgContainer.ARG_CALL_ACCEPTED, callAccepted ?: false)
                }

                NavigationManager.getManager()
                    .topNavController
                    .safeNavigate(R.id.meeraCallFragment, bundle = bundle)
            }
        }
    }

    private fun bindService() {
        signalingServiceConnectionWrapper.bindService(activity)
    }

    private fun unbindService() {
        signalingServiceConnectionWrapper.unbindService(activity)
    }

    private fun socketStateListenerForIncomingCall() {
        socket.onOpenSocket {
            checkServiceConnection()
        }
        socket.onErrorSocket { throwable, response ->
            handleSocketError()
        }
    }

    // socket already init and opened
    private fun socketStateListenerForOutgoingCall() {
        checkServiceConnection()
        socket.onErrorSocket { throwable, response ->
            handleSocketError()
        }
    }

    private fun checkServiceConnection() {
        activity.lifecycleScope.launch {
            signalingServiceConnectionWrapper.checkServiceConnection {
                Timber.e("On CALL Service connected!")
            }
        }
    }

    private fun handleSocketError() {
        if (signalingServiceConnectionWrapper.connectionEstablished.get()) {
            signalingServiceConnectionWrapper.connectionEstablished.set(false)
        }
    }

    /**
     * Запускаем звонки, когда приложение не активно и по входящему пушу
     * мы нажимаем "Начать звонок"
     */
    fun actionStartCallFromPush(extras: Bundle?) {
        signalingServiceConnectionWrapper.actionStartCall(extras) {
            shouldShowOnLockScreen(true)
        }
    }

    /**
     * Держать экран включенный/выключенный
     */
    private fun shouldShowOnLockScreen(shouldShow: Boolean) {
        if (shouldShow) {
            val wakelockFlags = PowerManager.ACQUIRE_CAUSES_WAKEUP or PowerManager.SCREEN_DIM_WAKE_LOCK
            val powerManager = activity.getSystemService(Context.POWER_SERVICE) as PowerManager
            wakeLock = powerManager.newWakeLock(wakelockFlags, WAKELOCK_TAG)
            wakeLock?.acquire()
        } else {
            wakeLock?.release()
            wakeLock = null
        }
    }

    private fun onSocketInitialized(block: () -> Unit) {
        socket.onSocketInitialized = { isInitialized ->
            if (isInitialized) block()
        }
    }

    private fun onGetCallFragment(
        onCallFragmentFound: (fragment: Fragment?, fragmentManager: FragmentManager) -> Unit
    ) {
        val navHost = NavigationManager.getManager().topNavHost
        val fragmentManager = navHost.childFragmentManager
        val callFragment = fragmentManager.fragments.find { it is MeeraCallFragment }
        onCallFragmentFound.invoke(callFragment, fragmentManager)
    }

}
