package com.numplates.nomera3.telecom

import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.lifecycle.LifecycleService
import com.google.gson.Gson
import com.meera.core.extensions.empty
import com.meera.core.extensions.fromJson
import com.meera.core.network.websocket.WebSocketMainChannel
import com.meera.core.preferences.AppSettings
import com.meera.db.models.dialog.DialogEntity
import com.meera.db.models.dialog.UserChat
import com.numplates.nomera3.ACTION_INCOMING_CALL
import com.numplates.nomera3.AUDIO_CALL_KEY
import com.numplates.nomera3.Act
import com.numplates.nomera3.App
import com.numplates.nomera3.CALL_ACTION_ACCEPT_CALL
import com.numplates.nomera3.CALL_ACTION_OPEN_CALL
import com.numplates.nomera3.CALL_ACTION_REJECT_CALL
import com.numplates.nomera3.TYPE_CALL_ACTION
import com.numplates.nomera3.data.fcm.models.PushCallObject
import com.numplates.nomera3.modules.analytics.domain.AnalyticsInteractor
import com.numplates.nomera3.modules.baseCore.helper.amplitude.AmplitudePropertyCallCanceller
import com.numplates.nomera3.presentation.router.IArgContainer
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_PUSH_OBJECT
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_ROOM_ID
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_USER_MODEL
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.appspot.apprtc.AppRTCClient
import org.webrtc.IceCandidate
import org.webrtc.PeerConnection
import org.webrtc.SessionDescription
import timber.log.Timber
import java.lang.ref.WeakReference
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject

/**
 * Detailed flow diagram can be found here: /NUMAD/schemes/signaling_service_diagram.jpg
 */
class SignalingService : LifecycleService(), AppRTCClient {

    var onStartCall: ((Boolean, UserChat?, Boolean?) -> Unit)? = null

    private val binder: IBinder = SignalingServiceBinder()
    private val disposables by lazy { CompositeDisposable() }
    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)
    private val pqMutex = Mutex()

    private val callState = AtomicReference(SignalingStates.SIGNALING_IDLE)
    private val isIncoming = AtomicBoolean(false)
    private val isStated = AtomicBoolean(false)
    private var webRTCClientNotification: AtomicReference<AppRTCClient.SignalingEvents>? =
        AtomicReference<AppRTCClient.SignalingEvents>(EmptyWebrtcSignaling())
    private var callProgressNotification: AtomicReference<CallProgressEvents>? =
        AtomicReference<CallProgressEvents>(EmptyCallProgress())

    private val inStateAction = mapOf(
        SignalingStates.SIGNALING_IDLE to ::inIdleState,
        SignalingStates.SIGNALING_INCOMING_CALL to ::inIncomingCallState,
        SignalingStates.SIGNALING_OUTGOING_CALL to ::inOutgoingCallState,
        SignalingStates.SIGNALING_WEBRTC_SESSION to ::inWebRtcSessionState
    )

    private val entryStateAction = mapOf(
        SignalingStates.SIGNALING_IDLE to ::entryIdleState,
        SignalingStates.SIGNALING_INCOMING_CALL to ::entryIncomingCallState,
        SignalingStates.SIGNALING_OUTGOING_CALL to ::entryOutgoingCallState,
        SignalingStates.SIGNALING_WEBRTC_SESSION to ::entryWebRtcSessionState
    )

    /**
     * Здесь используется кастомная реализация Java Priority Queue
     * для выполнения сетевых запросов по приоритетам.
     * */
    @Suppress("RemoveExplicitTypeArguments")
    private val eventPriorityQueue = MinHeapPQ<SignalingEvent> { a, b ->
        when {
            a.priority > b.priority -> 1
            a.priority < b.priority -> -1
            else -> 0
        }
    }

    /**
     * Очередь, которая собирает параметры для нескольких сетевых запросов
     * и далее передает их в [queueSignalingRequest]
     * */
    private var disposable: Disposable? = null
    private var txtQueue = mutableListOf<TxtQueueElement>()
    private var roomIdInProc: Long = 0L
    private var callUser: UserChat? = null
    private var uuidInProc: String = String.empty()
    private var videoCall: Boolean = true
    private var callAccepted: Boolean? = null

    @Inject
    lateinit var webSocketChannel: WebSocketMainChannel

    @Inject
    lateinit var gson: Gson

    @Inject
    lateinit var callNotificationManager: CallNotificationManager

    @Inject
    lateinit var callIntentProvider: CallIntentProvider

    @Inject
    lateinit var appSettings: AppSettings

    @Inject
    lateinit var tracker: AnalyticsInteractor

    @Inject
    lateinit var callWebSocketManager: CallWebSocketManager

    override fun onBind(intent: Intent): IBinder {
        (binder as? SignalingServiceBinder)?.setService(this)
        if (intent.hasExtra(ARG_USER_MODEL)) {
            callUser = intent.getParcelableExtra(ARG_USER_MODEL) as? UserChat
            roomIdInProc = intent.getLongExtra(ARG_ROOM_ID, 0)
            uuidInProc = intent.getStringExtra(IArgContainer.ARG_MESSAGE_ID) ?: ""
        }
        return binder
    }


    /**
     * Using with PUSH Notifications
     */
    @Suppress("MoveVariableDeclarationIntoWhen")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_INCOMING_CALL) {
            val pushCallObject = gson.fromJson(intent.getStringExtra(ARG_PUSH_OBJECT), PushCallObject::class.java)
            Timber.d("[onStartCommand] entry")
            roomIdInProc = pushCallObject.roomId?.toLong() ?: 0L
            uuidInProc = pushCallObject.messageId ?: String.empty()
            callUser = intent.getParcelableExtra(ARG_USER_MODEL) as? UserChat
            isIncoming.set(true)
            videoCall = false
            val typeCallAction = intent.extras?.getInt(TYPE_CALL_ACTION)
            when (typeCallAction) {
                CALL_ACTION_REJECT_CALL -> handleUserRejectedCall()
                CALL_ACTION_ACCEPT_CALL,
                CALL_ACTION_OPEN_CALL -> handleUsersIncomingCall(intent, typeCallAction)
            }
        }
        return START_STICKY
    }

    fun cancelCallNotification() = callNotificationManager.cancelCallNotification()

    /**
     * При нажатии кнопки Decline на пуше, мы подключаемся к ВебСокетам и
     * отправляем [callProgressReject], затем отключаемся от вебсокетов и
     * убиваем сервис
     * */
    private fun handleUserRejectedCall() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            callNotificationManager.cancelCallNotification()
        }
        tracker.logCallCancel(AmplitudePropertyCallCanceller.CALLED)
        callWebSocketManager.prepareSocketConnection(
            onReadyToUse = {
                Timber.d("Rejecting a call.")
                callProgressReject {
                    webSocketChannel.disconnectMainChannel()
                    webSocketChannel.disconnectSocket()
                    stopSelf()
                }
            },
            onError = {
                Timber.e("Couldn't prepare socket connection.")
                webSocketChannel.disconnectMainChannel()
                webSocketChannel.disconnectSocket()
                stopSelf()
            },
        )
    }

    private fun handleUsersIncomingCall(intent: Intent?, typeCallAction: Int) {
        Timber.d("[handleIncomingCall] Call accepted. callUser: ${callUser};")
        val localCallUser = callUser ?: return
        val pushCallUser = gson.fromJson(intent?.getStringExtra(ARG_PUSH_OBJECT), PushCallObject::class.java)
        callNotificationManager.cancelCallNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            callNotificationManager.createPushForIncomingCall(
                userChat = localCallUser,
                callPushObject = pushCallUser,
            )
        } else {
            callIntentProvider.createOpenCallIntent(
                typeCallAction = typeCallAction,
                userChat = localCallUser,
                pushObject = pushCallUser,
            ).let { callIntent -> startActivity(callIntent) }
        }
    }

    override fun onCreate() {
        App.component.inject(this)
        super.onCreate()
        lifecycle.addObserver(callWebSocketManager)
    }

    /**
     * Создаем и обнуляем [disposable], т.к.
     * он должен быть в единственном экземпляре,
     * вместо использования [CompositeDisposable]
     *
     *
     * [onDestroy] вызыввается при каждом сворачивании приложения,
     * при условии, что звонок не выполняется.
     * Мы в [Act.onStart] вызываем [bindService]
     * в [Act.onStop] - [unbindService]
     * */
    override fun onDestroy() {
        Timber.e("[onDestroy]")
        serviceJob.cancel()
        if (disposable != null && !disposable!!.isDisposed) {
            disposable!!.dispose()
            disposable = null
        }
        callProgressNotification = null
        webRTCClientNotification = null
        webSocketChannel.disconnectMainChannel()
        webSocketChannel.disconnectSocket()
        disposables.dispose()
        disposables.clear()
        super.onDestroy()
    }

    override fun onUnbind(intent: Intent?): Boolean {
        (binder as? SignalingServiceBinder)?.removeService()
        stopSelf()
        return super.onUnbind(intent)
    }

    /**
     * Start web socket observe again when become reconnect
     */
    fun observeSignalingChannel(block: () -> Unit) {
        Timber.e("CALL_SERVICE_LOG OBSERVE SignaLLING SERVICE >>>>>>>>>>>>>>>>>> ${disposable?.isDisposed}")
        if (disposable != null && !disposable!!.isDisposed) {
            disposable?.dispose()
            disposable = null
        }
        disposable = webSocketChannel.observeSignallingChannel()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ response ->
                val json = gson.toJson(response.payload)
                val fromRemote = gson.fromJson<SignalingMsgPayload>(json)
                Timber.d("CALL_SERVICE_LOG SIGNALLING_SERVICE_OBSERVER RESPONSE >>>>>>>>>>>>> TYPE(${fromRemote.type}) RESPONSE:${response.payload}")

                //
                // Call Progress
                //
                var roomId = 0L
                if (fromRemote.room?.roomId != null || fromRemote.room?.roomId != 0L) {
                    roomId = fromRemote.room!!.roomId
                } else if (fromRemote.roomId != null || fromRemote.roomId != 0L) {
                    roomId = fromRemote.roomId!!
                }

                Timber.e("ROOM_ID !!! $roomId")

                if (fromRemote.type == MessageType.INITIATE_CALL.type) {
                    callState.set(SignalingStates.SIGNALING_IDLE)
                    pushSignalingEvent(SignalingIncomingCall(fromRemote, SignalingEvent.Origin.ORIGIN_REMOTE))
                } else if (fromRemote.type == MessageType.ACCEPT_CALL.type) {
                    Timber.d(" observeSignalingChannel ACCEPT_CALL: ${fromRemote.extdata}")
                    pushSignalingEvent(SignalingAcceptCall(fromRemote.uuid, roomId))
                } else if (fromRemote.type == MessageType.REJECT_CALL.type) {
                    Timber.d(" observeSignalingChannel REJECT_CALL: ${fromRemote.extdata}")
                    pushSignalingEvent(SignalingRejectCall(fromRemote.uuid, roomId))
                } else if (fromRemote.type == MessageType.STOP_CALL.type) {
                    Timber.d(" observeSignalingChannel STOP_CALL: ${fromRemote.extdata}")
                    pushSignalingEvent(SignalingStopCall(fromRemote.uuid, roomId))
                } else if (fromRemote.type == MessageType.LINE_BUSY.type) {
                    Timber.d(" observeSignalingChannel LINE_BUSY: ${fromRemote.extdata}")
                    pushSignalingEvent(SignalingLineBusy(fromRemote.uuid, roomId))
                }

                //
                // WebRTC
                //
                else if (fromRemote.type == MessageType.OFFER.type) {
                    Timber.d(" observeSignalingChannel OFFER")
                    pushSignalingEvent(
                        SignalingOffer(
                            fromRemote.uuid,
                            roomId,
                            fromRemote.extdata,
                            SignalingEvent.Origin.ORIGIN_REMOTE
                        )
                    ) //TODO change data
                } else if (fromRemote.type == MessageType.ANSWER.type) {
                    Timber.d(" observeSignalingChannel ANSWER")
                    pushSignalingEvent(
                        SignalingAnswer(
                            fromRemote.uuid,
                            roomId,
                            fromRemote.extdata,
                            SignalingEvent.Origin.ORIGIN_REMOTE
                        )
                    )
                } else if (fromRemote.type == MessageType.CANDIDATES.type) {
                    Timber.d(" observeSignalingChannel CANDIDATES")
                    pushSignalingEvent(
                        SignalingCandidate(
                            fromRemote.uuid,
                            roomId,
                            fromRemote.extdata,
                            SignalingEvent.Origin.ORIGIN_REMOTE
                        )
                    )
                } else if (fromRemote.type == MessageType.CANDIDATES_REMOVE.type) {
                    Timber.d(" observeSignalingChannel CANDIDATES_REMOVE")
                    pushSignalingEvent(
                        SignalingRemoveCandidates(
                            fromRemote.uuid,
                            roomId,
                            fromRemote.extdata,
                            SignalingEvent.Origin.ORIGIN_REMOTE
                        )
                    )
                }
            }, { error -> Timber.e("ERROR: Observe Signaling: $error") })
        block()
    }

    /**
     * Сам по себе get_ice возвращает с нашего бэка массив со stun и turn серверами.
     * Они нужны для обмена сервисной информацией во время звонка peer-to-peer
     * */
    private fun getIceServersFromPrefs() {
        val prefIceServers = appSettings.prefCallIceServers(SignalingGetIceResponsePayload::class.java)
        Timber.d("SIGNALLING_SERVICE GetIce from prefs ----> $prefIceServers")

        val iceServers = mutableListOf<PeerConnection.IceServer>()

        prefIceServers?.stunServers?.let { servers ->
            for (stunUrl in servers) {
                iceServers.add(
                    PeerConnection.IceServer
                        .builder(stunUrl)
                        .createIceServer()
                )
            }
        }

        prefIceServers?.turnServers?.forEach { turn ->
            iceServers.add(
                PeerConnection.IceServer
                    .builder(turn.url)
                    .setUsername(turn.username)
                    .setPassword(turn.credential)
                    .createIceServer()
            )
        }

        // TODO: сделать доступ через Core->rxPreferences https://nomera.atlassian.net/browse/BR-17292
        appSettings.writeCallsRtcpMuxPolicy(prefIceServers?.rtcpMuxPolicy.orEmpty())
        appSettings.writeCallsTcpCandidatePolicy(prefIceServers?.tcpCandidatePolicy.orEmpty())

        val signalingParameters: AppRTCClient.SignalingParameters = AppRTCClient.SignalingParameters(
                iceServers,
                !isIncoming.get(),
                null,
                null,
                null,
                null,
                null
            )
        webRTCClientNotification?.get()?.onConnectedToRoom(signalingParameters)
    }

    private fun sendSignalingRequest(method: String, payload: Map<String, Any>) {
        Timber.e("[sendSignalingRequest] $method")
        txtQueue.add(TxtQueueElement(method, payload))
    }

    /**
     * Выполняет сетевой запрос, параметры которого берет из [txtQueue]
     * */
    private fun queueSignalingRequest() {

        txtQueue.forEach {
            Timber.d("[queueSignalingRequest] txtQueue: ${it.m}  payload: ${it.p}")
        }

        if (txtQueue.size == 0) {
            Timber.e("[queueSignalingRequest] nothing to send ...")
            return
        }

        val element: TxtQueueElement = txtQueue.removeAt(0)

        disposables.add(
            webSocketChannel.pushSignalingMessage(element.m, element.p)
                .flatMap { response ->
                    Observable.fromCallable {
                        Timber.e("SIGNALLING_SERVICE Push message M:${element.m} P:${element.p} ===> RESPONSE: ${response.payload}")
                        val json = gson.toJson(response.payload)
                        var more = true
                        if (element.m == MessageType.INITIATE_CALL.type) {
                            val msgResponse: SignalingStartCallResponse =
                                gson.fromJson<SignalingStartCallResponse>(json)
                            Timber.d("SIGNALLING_SERVICE INITIATE_CALL Response: ${msgResponse.response}")
                            storeOutgoingCallInfo(msgResponse.response.room_id, msgResponse.response.id)
                        } else {
                            val msgResponse = gson.fromJson<SignalingMsgResponse>(json)
                            Timber.d("SIGNALLING_SERVICE Response OTHER TYPES: $msgResponse isStated:${isStated.get()}")
                            if (isStated.get()) {
                                stopSelf()
                                more = false
                            }
                        }
                        if (more) {
                            Timber.d(" SEND_MORE: $json")
                            pushSignalingEvent(SignalingSendMore())
                        }
                    }
                }
                .subscribeOn(Schedulers.io())
                .subscribe({ rsp ->
                    Timber.d("queueSignalingRequest successfully - rsp $rsp")
                }, { error ->
                    Timber.e("ERROR queueSignalingRequest: $error")
                    // error.printStackTrace()
                    if (isStated.get()) {
                        stopSelf()
                    } else {
                        // TODO: either re-send or switch to IDLE
                        pushSignalingEvent(SignalingTransmissionError())
                    }
                })
        )
    }

    /**
     * Эта штука обновляет [roomIdInProc] и [uuidInProc]
     * с блокировкой потока при помощи [Mutex]
     * */
    private fun storeOutgoingCallInfo(rid: Long, uuid: String) {
        try {
            serviceScope.launch {
                Timber.d("SIGNALLING_SERVICE storeOutgoingCallInfo roomID:$rid UUID:$uuid")
                pqMutex.withLock {
                    roomIdInProc = rid
                    uuidInProc = uuid
                }
            }
        } catch (e: Exception) {
            Timber.d("SIGNALLING_SERVICE storeOutgoingCallInfo Exception: $e")
        }
    }

    /**
     * Здесь добавляется задача в [eventPriorityQueue] с блокировкой потока
     * и далее выполняется сетевой запрос [procSM] -> [queueSignalingRequest]
     * */
    private fun pushSignalingEvent(evt: SignalingEvent, callback: (() -> Unit)? = null) {
        Timber.e("[pushSignalingEvent] entry $evt")
        Timber.e("txtQueue $txtQueue")
        try {
            serviceScope.launch {
                pqMutex.withLock {
                    Timber.e("[pushSignalingEvent] into PQ")
                    eventPriorityQueue.insert(evt)
                }
                async {
                    pqMutex.withLock {
                        Timber.e("[[pushSignalingEvent] into PQ")
                        procSM(eventPriorityQueue.delMin())
                    }
                }.invokeOnCompletion {
                    callback?.invoke()
                }
            }
        } catch (e: Exception) {
            Timber.d("[pushSignalingEvent] Exception: $e")
        }
    }

    /**
     * Если [SignalingStates.SIGNALING_IDLE] то мы можем либо
     * позвонить -> [PlaceCall]
     * либо принять звонок -> [SignalingIncomingCall]
     * */
    private fun inIdleState(evt: SignalingEvent) {
        Timber.d("[inIdleState]: $evt")
        if (evt is PlaceCall) {
            if (evt.origin == SignalingEvent.Origin.ORIGIN_LOCAL) {
                callState.set(SignalingStates.SIGNALING_OUTGOING_CALL)
                isIncoming.set(false)
                callUser = evt.user
                sendSignalingRequest(MessageType.INITIATE_CALL.type, evt.toPayload())
                Timber.e("START_CALL ${evt.toPayload()}")
                return
            }
        } else if (evt is SignalingIncomingCall) {
            if (evt.origin == SignalingEvent.Origin.ORIGIN_REMOTE) {
                callState.set(SignalingStates.SIGNALING_INCOMING_CALL)
                isIncoming.set(true)
                roomIdInProc = evt.roomId
                uuidInProc = evt.uuid
                Timber.e("ROOM_ID IN_PROC = $roomIdInProc")
                if (callUser == null) {
                    callUser = evt.callUser
                }
                videoCall = evt.videoCall
                return
            }
        }
        Timber.d("[inIdleState] IGNORE: $evt")
    }

    /**
     * Здесь продолжается цепочка действий если звонок Входящий
     * [SignalingStates.SIGNALING_INCOMING_CALL]
     * */
    private fun inIncomingCallState(evt: SignalingEvent) {
        Timber.e("inIncommingCallState $evt ${evt.origin}")
        if (evt is SignalingIncomingCall) {
            if (evt.origin == SignalingEvent.Origin.ORIGIN_OUSIDE) {
                // TODO: Add to the notification queue info about rejected call
                //       call will be rejected in the broadcast receiver
                return
            }
            if (evt.origin == SignalingEvent.Origin.ORIGIN_REMOTE) {
                Timber.e("evt.origin == SignalingEvent.Origin.ORIGIN_REMOTE")
                return
            }
        } else if (evt is PlaceAccept) {
            Timber.e("evt is PlaceAccep!!!")
            callState.set(SignalingStates.SIGNALING_WEBRTC_SESSION)
            sendSignalingRequest(
                MessageType.ACCEPT_CALL.type,
                SignalingAcceptCall(uuidInProc, roomIdInProc).toPayload()
            )
            callProgressNotification?.get()?.callIsAccepted(roomIdInProc, uuidInProc)

            return
        } else if (evt is PlaceReject) {
            Timber.e("evt is PlaceReject")
            txtQueue.clear()
            callState.set(SignalingStates.SIGNALING_IDLE)
            sendSignalingRequest(
                MessageType.REJECT_CALL.type,
                SignalingRejectCall(uuidInProc, roomIdInProc).toPayload()
            )
            return
        }
        if (evt is SignalingStopCall) {
            Timber.e("evt is SignalingRejectCall")
            txtQueue.clear()
            callState.set(SignalingStates.SIGNALING_IDLE)
            callProgressNotification?.get()?.callIsStopped()
            return
        } else if (evt is SignalingTransmissionError) {
            Timber.e("evt is SignalingTransmissionError")
            txtQueue.clear()
            callState.set(SignalingStates.SIGNALING_IDLE)
            return
        } else if (evt is SignalingRejectCall) {
            Timber.d("CALL_REJECTED")
            txtQueue.clear()
            callState.set(SignalingStates.SIGNALING_IDLE)
            callProgressNotification?.get()?.callIsRejected()
        }

        Timber.d("[inIncommingCallState] IGNORE: $evt")
    }

    /**
     * Здесь продолжается цепочка действий если звонок Исходящий
     * [SignalingStates.SIGNALING_OUTGOING_CALL]
     * */
    private fun inOutgoingCallState(evt: SignalingEvent) {
        Timber.d("SIGNALLING_SERVICE inOutgoingCallState $evt ${evt.origin}")
        if (evt is SignalingIncomingCall) {
            if (evt.origin == SignalingEvent.Origin.ORIGIN_OUSIDE) {
                // TODO: Add to the notification queue info about rejected call
                //       call will be rejected in the broadcast receiver
                callState.set(SignalingStates.SIGNALING_IDLE)
                return
            }
            if (evt.origin == SignalingEvent.Origin.ORIGIN_REMOTE) {
                callState.set(SignalingStates.SIGNALING_IDLE)
                return
            }
            return
        } else if (evt is SignalingLineBusy) {
            Timber.e("evt is SignalingLineBusy")
            txtQueue.clear()
            callState.set(SignalingStates.SIGNALING_IDLE)
            callProgressNotification?.get()?.lineIsBusy(/*may be some notes*/)
        } else if (evt is SignalingRejectCall) {
            Timber.e("evt is SignalingRejectCall")
            txtQueue.clear()
            callState.set(SignalingStates.SIGNALING_IDLE)
            callProgressNotification?.get()?.callIsRejected(/*may be some notes*/)
        } else if (evt is SignalingAcceptCall) {
            Timber.e("evt is SignalingAcceptCall")
            callState.set(SignalingStates.SIGNALING_WEBRTC_SESSION)
            callProgressNotification?.get()?.callIsAccepted(roomIdInProc, uuidInProc)
        } else if (evt is PlaceStop) {
            Timber.e("inOutgoingCallState evt is PlaceStop")
            txtQueue.clear()
            callState.set(SignalingStates.SIGNALING_IDLE)
            // TODO: attention, should be 'STOP_CALL' but currently 'REJECT_CALL' is used ...
            sendSignalingRequest(MessageType.STOP_CALL.type, SignalingRejectCall(uuidInProc, roomIdInProc).toPayload())
            queueSignalingRequest()
            return
        } else if (evt is SignalingTransmissionError) {
            Timber.e("evt is SignalingTransmissionError")
            txtQueue.clear()
            callState.set(SignalingStates.SIGNALING_IDLE)
        }
        Timber.d("[inOutgoingCallState] IGNORE: $evt")
    }

    /**
     * Здесь подключается в работу [AppRTCClient]
     * если стэйт [SignalingStates.SIGNALING_WEBRTC_SESSION]
     * */
    private fun inWebRtcSessionState(evt: SignalingEvent) {

        Timber.d(" WEB_RTC_SESSION_STATE: $evt")

        if (evt is SignalingIncomingCall) {
            Timber.e("evt is SignalingIncommingCall")

            if (evt.origin == SignalingEvent.Origin.ORIGIN_OUSIDE) {
                // TODO: Add to the notification queue info about rejected call
                //       call will be rejected in the broadcast receiver
                Timber.e("evt.origin == SignalingEvent.Origin.ORIGIN_OUSIDE")
                callState.set(SignalingStates.SIGNALING_IDLE)
                return
            }
            if (evt.origin == SignalingEvent.Origin.ORIGIN_REMOTE) {
                sendSignalingRequest(MessageType.LINE_BUSY.type, evt.toPayload())
                Timber.e("evt.origin == SignalingEvent.Origin.ORIGIN_REMOTE")
                callState.set(SignalingStates.SIGNALING_IDLE)
                return
            }
        } else if (evt is SignalingStopCall) {
            Timber.e("evt is SignalingStopCall")
            if (evt.origin == SignalingEvent.Origin.ORIGIN_REMOTE) {
                callState.set(SignalingStates.SIGNALING_IDLE)
                webRTCClientNotification?.get()?.onChannelClose()
                Timber.e("evt.origin == SignalingEvent.Origin.ORIGIN_REMOTE")
                return
            }
        } else if (evt is PlaceStop) {
            callState.set(SignalingStates.SIGNALING_IDLE)
            sendSignalingRequest(MessageType.STOP_CALL.type, SignalingStopCall(uuidInProc, roomIdInProc).toPayload())
            Timber.e("inWebRtcSessionState evt is PlaceStop")
            return
        }
        //
        // webrtc
        //
        else if (evt is SignalingOffer) {
            Timber.e("evt is SignalingOffer ${evt.data}")
            if (evt.origin == SignalingEvent.Origin.ORIGIN_LOCAL) {
                sendSignalingRequest(
                    MessageType.OFFER.type, SignalingOffer(
                        uuidInProc,
                        roomIdInProc,
                        evt.data,
                        SignalingEvent.Origin.ORIGIN_LOCAL
                    ).toPayload()
                )
                Timber.e("evt.origin == SignalingEvent.Origin.ORIGIN_LOCAL")
                return
            }
            if (evt.origin == SignalingEvent.Origin.ORIGIN_REMOTE) {
                webRTCClientNotification?.get()?.onRemoteDescription(
                    SessionDescription(
                        SessionDescription.Type.OFFER,
                        evt.data
                    )
                )
                Timber.e("evt.origin == SignalingEvent.Origin.ORIGIN_REMOTE")
                return
            }
        } else if (evt is SignalingAnswer) {
            Timber.e("evt is SignalingAnswer")
            if (evt.origin == SignalingEvent.Origin.ORIGIN_LOCAL) {
                sendSignalingRequest(
                    MessageType.ANSWER.type, SignalingAnswer(
                        uuidInProc, roomIdInProc, evt.data, SignalingEvent.Origin.ORIGIN_LOCAL
                    ).toPayload()
                )
                Timber.e("evt.origin == SignalingEvent.Origin.ORIGIN_LOCAL")
                return
            }
            if (evt.origin == SignalingEvent.Origin.ORIGIN_REMOTE) {
                webRTCClientNotification?.get()
                    ?.onRemoteDescription(SessionDescription(SessionDescription.Type.ANSWER, evt.data))
                Timber.e("evt.origin == SignalingEvent.Origin.ORIGIN_REMOTE")
                return
            }
        } else if (evt is SignalingCandidate) {
            Timber.d("SIGNALLING_SERVICE event is SignallingCandidate (${evt})")
            if (evt.origin == SignalingEvent.Origin.ORIGIN_LOCAL) {
                sendSignalingRequest(
                    MessageType.CANDIDATES.type,
                    SignalingCandidate(
                        uuidInProc,
                        roomIdInProc,
                        evt.data,
                        SignalingEvent.Origin.ORIGIN_LOCAL
                    ).toPayload()
                )
                Timber.d("evt.origin == SignalingEvent.Origin.ORIGIN_LOCAL")
                return
            }
            if (evt.origin == SignalingEvent.Origin.ORIGIN_REMOTE) {
                val list = gson.fromJson<List<IceCandidate>>(evt.data)
                for (l in list) {
                    webRTCClientNotification?.get()?.onRemoteIceCandidate(l)
                }
                Timber.d("evt.origin == SignalingEvent.Origin.ORIGIN_REMOTE")
                return
            }
        } else if (evt is SignalingRemoveCandidates) {
            Timber.d(" EVENT_DATA SignalingRemoveCandidates: ${evt.data}")
            Timber.e("evt is SignalingRemoveCandidates")
            if (evt.origin == SignalingEvent.Origin.ORIGIN_LOCAL) {
                sendSignalingRequest(
                    MessageType.CANDIDATES_REMOVE.type,
                    SignalingRemoveCandidates(
                        uuidInProc,
                        roomIdInProc,
                        evt.data,
                        SignalingEvent.Origin.ORIGIN_LOCAL
                    ).toPayload()
                )
                Timber.e("evt.origin == SignalingEvent.Origin.ORIGIN_LOCAL")
                return
            }
            if (evt.origin == SignalingEvent.Origin.ORIGIN_REMOTE) {
                val candidates: Array<IceCandidate> = gson.fromJson<Array<IceCandidate>>(evt.data)
                webRTCClientNotification?.get()?.onRemoteIceCandidatesRemoved(candidates)
                Timber.e("evt.origin == SignalingEvent.Origin.ORIGIN_REMOTE ${evt.data}")
                return
            }
        } else if (evt is SignalingTransmissionError) {
            Timber.e("evt is SignalingTransmissionError")
            callState.set(SignalingStates.SIGNALING_IDLE)
        }

        Timber.d("[inWebRtcSessionState] IGNORE: ${evt.toPayload()}")
    }

    private fun entryIdleState() {
        Timber.d("[entryIdleState]")
        txtQueue.clear()
    }

    /**
     * Здесь мы вызываем экран звонилки [CallFragment] из [Act]
     * */
    private fun entryIncomingCallState() {
        Timber.d("CALL_LOG [entryIncomingCallState] ENTRY_INCOMING_CALL User:$callUser")
        invokeCallProgressUI(true, callUser)
    }

    private fun entryOutgoingCallState() {
        Timber.d("[entryOutgoingCallState]")
        invokeCallProgressUI(false, callUser)
    }

    private fun entryWebRtcSessionState() {
        Timber.d("[entryWebRtcSessionState]")
        // invoke webrtc client activity
    }


    /**
     * Здесь проверяем текущий [SignalingEvent]
     * и выполняем сетевой запрос [queueSignalingRequest]
     * */
    private fun procSM(evt: SignalingEvent) {
        Timber.e("----------------- PS procSM -------------------")

        // horizontal evts:

        if (evt is SignalingGetIceServers) {
            Timber.e("SignalingGetIceServers")
            getIceServersFromPrefs()
            return
        }
        if (evt is SignalingSendMore) {
            Timber.e("SignalingSendMore")
            queueSignalingRequest()
            return
        }

        // vertical evts:

        val oldState: SignalingStates = callState.get()

        Timber.e("[procSM] INSTATE:  $callState")

        (inStateAction[callState.get()] ?: error("Unknown callState: $callState")).invoke(evt)

        if (oldState != callState.get()) {
            (entryStateAction[callState.get()] ?: error("Unknown callState: $callState")).invoke()
        }

        // send message to remote:
        queueSignalingRequest()

        Timber.e("[procSM] done.")
    }

    //
    // CALL PROGRESS UI
    //
    private fun invokeCallProgressUI(isIncoming: Boolean, callUser: UserChat?) {
        onStartCall?.invoke(isIncoming, callUser, callAccepted)
        this.callUser = null
        callAccepted = null
    }

    //
    // Activity API: Call Progress Part
    //

    fun registerCallProgressClient(client: CallProgressEvents) {
        callProgressNotification?.set(client)
    }

    fun cmdPlaceCall(
        callUser: UserChat,
        isIncoming: Boolean,
        callAccepted: Boolean?,
        roomId: Long?,
        messageId: String?
    ) {
        Timber.d(" callState: $callState")
        callState.set(SignalingStates.SIGNALING_IDLE)
        this.isIncoming.set(isIncoming)
        Timber.d("IS_INCOMING $isIncoming")
        this.callAccepted = callAccepted
        videoCall = false

        if (!isIncoming) {
            pushSignalingEvent(PlaceCall(callUser, AUDIO_CALL_KEY))
        } else {
            if (roomId != null && messageId != null) {
                val remoteObject = SignalingMsgPayload(
                    messageId,
                    roomId,
                    DialogEntity(),
                    "voip",
                    AUDIO_CALL_KEY
                )
                pushSignalingEvent(SignalingIncomingCall(remoteObject, SignalingEvent.Origin.ORIGIN_REMOTE))
            }
        }
    }

    fun callProgressStop() {
        Timber.e("[callProgressStop]")

        sendSignalingRequest(MessageType.STOP_CALL.type, SignalingStopCall(uuidInProc, roomIdInProc).toPayload())
        queueSignalingRequest()
        pushSignalingEvent(PlaceStop())
    }

    fun callProgressAccept() {
        Timber.e("[callProgressAccept]")
        pushSignalingEvent(PlaceAccept())
    }

    fun callProgressReject(callback: (() -> Unit)? = null) {
        Timber.e("[callProgressReject]")
        sendSignalingRequest(MessageType.REJECT_CALL.type, SignalingRejectCall(uuidInProc, roomIdInProc).toPayload())
        queueSignalingRequest()

        pushSignalingEvent(PlaceReject(), callback)
    }

    //
    // Activity API: WebRTC Part
    //

    fun registerWebRtcClient(client: AppRTCClient.SignalingEvents) {
        webRTCClientNotification?.set(client)
    }

    override fun connectToRoom(connectionParameters: AppRTCClient.RoomConnectionParameters) {
        Timber.e("[connectToRoom]")
        pushSignalingEvent(SignalingGetIceServers())
    }

    override fun sendOfferSdp(sdp: SessionDescription) {
        Timber.e("[sendOfferSdp]")
        pushSignalingEvent(SignalingOffer(String.empty(), 0, sdp.description, SignalingEvent.Origin.ORIGIN_LOCAL))
    }

    override fun sendAnswerSdp(sdp: SessionDescription) {
        Timber.e("[sendAnswerSdp]")
        pushSignalingEvent(SignalingAnswer(String.empty(), 0, sdp.description, SignalingEvent.Origin.ORIGIN_LOCAL))
    }

    override fun sendLocalIceCandidate(candidate: IceCandidate) {
        Timber.e("[sendLocalIceCandidate]")
        val list = listOf(candidate)
        pushSignalingEvent(
            SignalingCandidate(
                String.empty(),
                0,
                gson.toJson(list).toString(),
                SignalingEvent.Origin.ORIGIN_LOCAL
            )
        )
    }

    override fun sendLocalIceCandidateRemovals(candidates: Array<IceCandidate>) {
        Timber.e("[sendLocalIceCandidateRemovals]")
        pushSignalingEvent(
            SignalingRemoveCandidates(
                String.empty(),
                0,
                gson.toJson(candidates).toString(),
                SignalingEvent.Origin.ORIGIN_LOCAL
            )
        )
    }

    override fun disconnectFromRoom() {
        Timber.e("[disconnectFromRoom]")
        webRTCClientNotification?.set(EmptyWebrtcSignaling())
        pushSignalingEvent(PlaceStop())
    }
}

class SignalingServiceBinder : Binder() {
    private var service: WeakReference<SignalingService>? = null

    fun getService() = service

    fun setService(signalingService: SignalingService) {
        service = WeakReference(signalingService)
    }

    fun removeService() {
        service?.clear()
    }
}
