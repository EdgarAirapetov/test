package com.meera.core.network.websocket

import android.content.Context
import android.os.Build
import androidx.collection.ArraySet
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.google.gson.Gson
import com.meera.core.common.PLATFORM_NAME
import com.meera.core.extensions.getAppVersionName
import com.meera.core.extensions.getTimeZone
import com.meera.core.network.HTTP_CODE_FORBIDDEN
import com.meera.core.network.HTTP_CODE_NOT_AUTHORIZED
import com.meera.core.network.utils.BaseUrlManager
import com.meera.core.preferences.AppSettings
import com.meera.core.utils.HardwareIdUtil
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.ReplaySubject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Response
import org.phoenixframework.Channel
import org.phoenixframework.Message
import org.phoenixframework.Payload
import org.phoenixframework.Socket
import timber.log.Timber
import java.io.IOException
import java.net.Proxy
import java.util.Collections
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlin.properties.Delegates


@Singleton
class WebSocketMainChannel @Inject constructor(
    private val context: Context,
    private val baseUrlManager: BaseUrlManager,
    private val hardwareUtil: HardwareIdUtil,
    private val chuckerInterceptor: ChuckerInterceptor,
    private val appSettings: AppSettings
) {

    interface WebSocketConnectionListener {
        fun connectionStatus(isConnected: Boolean)
    }

    interface OnActivityInteractionCallback {
        fun onErrorHandler(responseError: Payload)
        fun onSocketPhxError(response: Response?)
    }

    val publishSocketConnection: PublishSubject<Boolean> = PublishSubject.create()
    val publishSocketStatus: BehaviorSubject<ConnectionStatus> = BehaviorSubject.create()

    private lateinit var socket: Socket
    private lateinit var mainChannel: Channel
    private lateinit var activityInteractionCallback: OnActivityInteractionCallback

    private val connectionListeners = Collections.synchronizedSet(ArraySet<WebSocketConnectionListener>())
    private val subscriptionNewPostSubject = ReplaySubject.create<Message>()
    private val subscriptionNewMomentSubject = ReplaySubject.create<Message>()

    private var socketConnectionObserverDelegate: Boolean by Delegates.observable(false) { _, old, new ->
        if (old != new) {
            connectionListeners.forEach { listener -> listener.connectionStatus(new) }
        }
    }

    var onSocketInitialized: (isInitialized: Boolean) -> Unit = {}

    private var isSocketInitialized: Boolean by Delegates.observable(false) { _, old, new ->
        onSocketInitialized.invoke(new)
    }

    fun addWebSocketConnectionListener(listener: WebSocketConnectionListener?) {
        connectionListeners.add(listener)
    }

    fun removeWebSocketConnectionListener(listener: WebSocketConnectionListener?) {
        connectionListeners.remove(listener)
    }

    fun initSocket(token: String) {
        val device = Build.MANUFACTURER + " - " + Build.MODEL
        val params = hashMapOf<String, Any>(
            "token" to token,
            "n-device" to device,
            "n-build" to context.getAppVersionName(),
            "n-os" to PLATFORM_NAME,
            "n-device-id" to hardwareUtil.getHardwareId(),
            "n-timezone" to getTimeZone(),
            "n-locale" to appSettings.locale,
            "n-session-id" to appSettings.sessionCounter,
        )

        Timber.e("Access token:$token UserID:${appSettings.readUID()} PARAM:$params")
        val client = OkHttpClient.Builder()
            .addInterceptor(chuckerInterceptor)
            .proxy(Proxy.NO_PROXY)
            .connectTimeout(1, TimeUnit.MINUTES)
            .build()
        socket = Socket(baseUrlManager.provideBaseUrlSocket(), params, client = client)
        setupLogger(socket)
        isSocketInitialized = true
    }

    fun errorHandler(activityCallback: OnActivityInteractionCallback) {
        this.activityInteractionCallback = activityCallback
    }

    fun connectSocket() {
        if (!socket.isConnected) {
            Timber.d("Connect socket")
            socket.connect()
        }
    }

    fun disconnectSocket() {
        if (::socket.isInitialized) {
            Timber.d("WS Disconnect socket")
            mainChannel.on(Channel.Event.CLOSE) {
                Timber.d("Channel PHX CLOSED")
            }
            socket.disconnect()
        }
    }

    fun onOpenSocket(block: () -> Unit) {
        socket.onOpen {
            block()
        }
    }

    fun onCloseSocket(block: () -> Unit) {
        socket.onClose {
            block()
        }
    }

    fun onJoinSocket(block: () -> Unit) {
        mainChannel.on(Channel.Event.JOIN) {
            block()
        }
    }

    fun onErrorSocket(block: (Throwable, Response?) -> Unit) {
        socket.onError { throwable, response ->
            block(throwable, response)
        }
    }

    fun createCommonRequest(method: String, payload: Map<String, Any>): Observable<Message> =
        commonRequest(method, payload)

    fun rejoinChannel(onSuccess: () -> Unit, onError: (String) -> Unit = {}) {
        mainChannel = socket.channel("main")
        mainChannel.join()
            .receive(STATUS_OK) {
                Timber.d("MAIN_CHANNEL JOINED")
                publishSocketStatus.onNext(ConnectionStatus.OnChannelJoined(true))
                socketConnectionObserverDelegate = true
                onSuccess.invoke()
            }
            .receive(STATUS_ERROR) {
                Timber.e("Failed to join chat room")
                publishSocketStatus.onNext(ConnectionStatus.OnChannelJoined(false))
                socketConnectionObserverDelegate = false
                onError.invoke(it.toString())
            }
        setupStartListeners()
        socket.connect()
    }

    /**
     * Disconnect socket and leave channel
     */
    fun disconnectAll() {
        if (::socket.isInitialized && ::mainChannel.isInitialized) {
            socket.remove(mainChannel)
        }
        disconnectMainChannel()
        disconnectSocket()
        publishSocketStatus.onNext(ConnectionStatus.OnChannelJoined(false))
        socketConnectionObserverDelegate = false
    }


    fun disconnectMainChannel() {
        Timber.e("DISCONNECT Main channel >>>>>>>>>>>>>>>>>>>>>>>>>>")
        if (::mainChannel.isInitialized) {
            mainChannel.leave()
        }
    }

    fun isConnected(): Boolean {
        return if (::socket.isInitialized) {
            socket.isConnected
        } else false
    }

    fun isInitialized(): Boolean {
        return ::socket.isInitialized
    }


    fun isChannelJoined(): Boolean {
        return if (::mainChannel.isInitialized) {
            mainChannel.isJoined
        } else false
    }

    /**
     * For group chat
     */
    fun pushCreateRoom(payload: Map<String, Any>) =
        commonRequest(REQUEST_CREATE_ROOM, payload)


    /**
     * Create new message - FIRST message (and create room)
     */
    fun pushNewMessage(payload: Map<String, Any>): Observable<Message> {
        Timber.d("PUSH new message to Server: Socket-isClosed: ${mainChannel.isClosed} Socket-isError: ${mainChannel.isErrored}")
        return Observable.create { emitter ->
            if (mainChannel.isErrored) {
                emitter.onError(WebSocketResponseException("ERROR: Socket is closed"))
            }

            mainChannel.push(REQUEST_NEW_MESSAGE, payload)
                .receive(STATUS_OK) { message ->
                    Timber.d("Response after push (new_message): ${message.payload}")
                    emitter.onNext(message)
                }
                .receive(STATUS_ERROR) { message ->
                    Timber.e("ERROR response after push (new_message): ${message.payload}")
                    emitter.onNext(message)
                }
        }
    }

    @Deprecated("Use REST method")
    suspend fun pushNewMessageCoroutine(payload: Map<String, Any>) =
        suspendCoroutine<Message> { continuation ->
            if (mainChannel.isErrored || mainChannel.isClosed) {
                continuation.resume(Message("", "", REQUEST_NEW_MESSAGE, hashMapOf("status" to "error"), ""))
            }

            mainChannel.push(REQUEST_NEW_MESSAGE, payload)
                .receive(STATUS_OK) { message ->
                    Timber.d("(Coroutine) Response after push (new_message): ${message.payload}")
                    continuation.resume(message)
                }
                .receive(STATUS_ERROR) { message ->
                    Timber.e("(Coroutine) ERROR response after push (new_message): ${message.payload}")
                    continuation.resume(message)
                }
        }

    fun getSubscriptionNewPostMessages(): Observable<Message> {
        return subscriptionNewPostSubject
    }

    fun getSubscriptionNewMomentMessages(): Observable<Message> {
        return subscriptionNewMomentSubject
    }

    /**
     * Remove message from server
     */
    fun pushRemoveMessage(payload: Map<String, Any>) =
        commonRequest(REQUEST_REMOVE_MESSAGE, payload)

    suspend fun pushRemoveMessageSuspend(payload: Map<String, Any>): Message {
        return suspendCoroutine<Message> { continuation ->
            mainChannel.push(REQUEST_REMOVE_MESSAGE, payload)
                .receive(STATUS_OK) { message ->
                    Timber.d("Response after push (remove_message): ${message.payload}")
                    continuation.resume(message)
                }
                .receive(STATUS_ERROR) { message ->
                    Timber.e("ERROR response after push (remove_message): ${message.payload}")
                    continuation.resume(message)
                }
        }
    }

    suspend fun pushGetMessagesSuspend(payload: Map<String, Any>): Message {
        return suspendCoroutine<Message> { continuation ->
            mainChannel.push(REQUEST_GET_MESSAGES, payload)
                .receive(STATUS_OK) { message ->
                    Timber.d("Response after push (get_messages): ${message.payload}")
                    continuation.resume(message)
                }
                .receive(STATUS_ERROR) { message ->
                    Timber.e("ERROR response after push (get_messages): ${message.payload}")
                    continuation.resume(message)
                }
        }
    }

    /**
     * Observe reload dialogs event
     */
    fun observeReloadDialogs(): Observable<Message> {
        Timber.d("Start Reload dialogs")
        return Observable.create { emitter ->
            mainChannel.on(REQUEST_RELOAD_DIALOGS) { message ->
                Timber.d("(On) -> Observe RELOAD_DIALOGS: ${Gson().toJson(message.payload)}")
                if (!emitter.isDisposed) emitter.onNext(message)
            }
        }
    }

    /**
     * Observe Receive new message from
     */
    fun observeGetMessages(): Observable<Message> {
        Timber.e("Start Observe MSG!")
        return Observable.create { emitter ->
            mainChannel.on(REQUEST_NEW_MESSAGE) { message ->
                Timber.e("(On) -> Observe GET_MESSAGES: ${Gson().toJson(message.payload)}")
                if (!emitter.isDisposed) emitter.onNext(message)
            }
        }
    }

    fun observeIncomingMessage(): Flow<Message> = callbackFlow {
        mainChannel.on(REQUEST_NEW_MESSAGE) { message ->
            Timber.e("Socket FLOW -> Observe Incoming messages: ${message.payload}")
            trySend(message)
        }
        awaitClose()
    }

    fun observeProfileStatistics(): Observable<Message> {
        Timber.e("Start observe PROFILE STATISTICS")
        return Observable.create { emitter ->
            mainChannel.on(REQUEST_PROFILE_STATISTICS) { message ->
                Timber.e("(On) -> Observe PROFILE STATISTICS: ${Gson().toJson(message.payload)}")
                if (!emitter.isDisposed) emitter.onNext(message)
            }
        }
    }

    /**
     * Observe update user if call toggle change state
     */
    fun observeUpdateUser(): Observable<Message> = Observable.create { emitter ->
        mainChannel.on(REQUEST_UPDATE_USER) { message ->
            Timber.d("(On) -> Observe update_user: ${Gson().toJson(message.payload)}")
            emitter.onNext(message)
        }
    }

    /**
     * Send event if user typing
     */
    fun pushTyping(payload: Map<String, Any>) =
        commonRequest(REQUEST_TYPING, payload)

    suspend fun pushTypingSuspend(payload: Map<String, Any>): Message {
        return suspendCoroutine<Message> { continuation ->
            mainChannel.push(REQUEST_TYPING, payload)
                .receive(STATUS_OK) { message ->
                    Timber.d("Response after push (typing): ${message.payload}")
                    continuation.resume(message)
                }
                .receive(STATUS_ERROR) { message ->
                    Timber.e("ERROR response after push (typing): ${message.payload}")
                    continuation.resume(message)
                }
        }
    }

    /**
     * Observe disconnect when reason exists (for exam. Token expires)
     */
    fun observeDisconnect(): Observable<Message> {
        Timber.d("Start Observable Disconnect")
        return Observable.create { emitter ->
            mainChannel.on("disconnect") { message ->
                Timber.d("Observe Disconnect when token expired")
                if (!emitter.isDisposed) emitter.onNext(message)
            }
        }
    }


    fun observeTypingFlow(): Flow<Message> = callbackFlow<Message> {
        mainChannel.on(REQUEST_TYPING) { message ->
            trySend(message).isSuccess
        }
        awaitClose { /** do nothing */ }
    }

    fun observeOnlineFlow(): Flow<Message> = callbackFlow {
        mainChannel.on(REQUEST_ONLINE) { message ->
            trySend(message).isSuccess
        }
        awaitClose { }
    }

    fun observableReactiveEventsUpdates(): Observable<Message> =
        Observable.create {
            mainChannel.on(REQUEST_NOTIFICATION_UPDATES) { message ->
                it.onNext(message)
            }
        }

    fun observeNotificationsUpdate(): Flow<Message> = callbackFlow {
        mainChannel.on(REQUEST_NOTIFICATION_UPDATES) { message ->
            trySend(message).isSuccess
        }
        awaitClose { }
    }

    fun pushEventsUpdates(
        createdAt: Long? = null
    ): Single<Message> =
        Single.create { emitter ->
            val payload = hashMapOf(
                "ts" to createdAt
            )
            mainChannel.push(REQUEST_SYNC_EVENTS_UPDATE, payload)
                .receive(STATUS_OK) { message ->
                    Timber.d("sync_events_update: REQUEST_SYNC_EVENTS_UPDATE socket - ${message.payload}")
                    emitter.onSuccess(message)
                }
                .receive(STATUS_ERROR) { message ->
                    emitter.onError(Exception(message.payload.toString()))
                    Timber.e("sync_events_update response after push (REQUEST_SYNC_EVENTS_UPDATE): ${message.payload}")
                }
        }

    @Deprecated("use rest api /v2/rooms")
    suspend fun pushGetRoomsSuspend(payload: Map<String, Any>) =
        suspendCoroutine<Message> { continuation ->
            if (::mainChannel.isInitialized) {
                mainChannel.push(REQUEST_GET_ROOMS, payload)
                    .receive(STATUS_OK) { message ->
                        Timber.d("(Coroutine) Response after push (get_rooms): ${message.payload}")
                        continuation.resume(message)
                    }
                    .receive(STATUS_ERROR) { message ->
                        Timber.e("(Coroutine) ERROR response after push (get_rooms): ${message.payload}")
                        continuation.resume(message)
                    }
            }
        }

    /**
     * When enter a room we should subscribe for receive room events (online, typing e.t.c)
     */
    suspend fun pushRoomSubscribe(payload: Map<String, Any>) =
        suspendCoroutine<Message> { continuation ->
            if (::mainChannel.isInitialized) {
                mainChannel.push("room_subscribe", payload)
                    .receive(STATUS_OK) { message ->
                        Timber.e("(Coroutine) Response after push (room_subscribe): ${message.payload}")
                        continuation.resume(message)
                    }
                    .receive(STATUS_ERROR) { message ->
                        Timber.e("(Coroutine) ERROR response after push (room_subscribe): ${message.payload}")
                        continuation.resume(message)
                    }
            }
        }

    /**
     * When exit a room we should unsubscribe from receiving events (online, typing e.t.c)
     */
    suspend fun pushRoomUnSubscribe(payload: Map<String, Any>) =
        suspendCoroutine<Message> { continuation ->
            if (::mainChannel.isInitialized) {
                mainChannel.push("room_unsubscribe", payload)
                    .receive(STATUS_OK) { message ->
                        Timber.d("(Coroutine) Response after push (room_unsubscribe): ${message.payload}")
                        continuation.resume(message)
                    }
                    .receive(STATUS_ERROR) { message ->
                        Timber.e("(Coroutine) ERROR response after push (room_unsubscribe): ${message.payload}")
                        continuation.resume(message)
                    }
            }
        }

    /**
     * Delete room from server
     */
    fun pushDeleteRoom(payload: Map<String, Any>) =
        commonRequest(REQUEST_DELETE_ROOM, payload)

    suspend fun pushDeleteRoomSuspend(payload: Map<String, Any>) =
        commonRequestSuspended(REQUEST_DELETE_ROOM, payload)

    /**
     * Get room members all data
     */
    fun pushGetMembers(payload: Map<String, Any>) =
        commonRequest(REQUEST_GET_MEMBERS, payload)


    fun pushGetGroupMembers(payload: Map<String, Any>): Observable<Message> {
        return Observable.create { emitter ->
            mainChannel.push(REQUEST_GET_MEMBERS, payload)
                .receive(STATUS_OK) { message ->
                    Timber.d("(NEW)Response after push ($REQUEST_GET_MEMBERS): ${message.payload}")
                    if (!emitter.isDisposed) {
                        emitter.onNext(message)
                    }
                }
                .receive(STATUS_ERROR) { message ->
                    Timber.e("ERROR (room not found get_members)")
                    if (!emitter.isDisposed) {
                        emitter.onNext(message)
                    }
                }
        }
    }

    /**
     * Get list admins in a group chat
     */
    suspend fun pushGetAdmins(payload: Map<String, Any>) =
        suspendCoroutine<Message> { continuation ->
            mainChannel.push(REQUEST_GET_ADMINS, payload)
                .receive(STATUS_OK) { message ->
                    Timber.d("(Coroutine) Response after push (get_admins): ${message.payload}")
                    continuation.resume(message)
                }
                .receive(STATUS_ERROR) { message ->
                    Timber.e("(Coroutine) ERROR response after push (get_admins): ${message.payload}")
                    continuation.resume(message)
                }
        }

    /**
     * Get list admins (include creator) and members in a group chat
     */
    suspend fun pushGetRoomGroupStatus(payload: Map<String, Any>) =
        suspendCoroutine<Message> { continuation ->
            mainChannel.push(REQUEST_GET_ROOM_GROUP_STATUS, payload)
                .receive(STATUS_OK) { message ->
                    Timber.d("(Coroutine) Response after push (get_group_room_status): ${message.payload}")
                    continuation.resume(message)
                }
                .receive(STATUS_ERROR) { message ->
                    Timber.e("(Coroutine) ERROR response after push (get_group_room_status): ${message.payload}")
                    continuation.resume(message)
                }
        }

    fun observeShakeFriendRequests(): Flow<Message> {
        return callbackFlow {
            mainChannel.on(REQUEST_SHAKE) { message ->
                trySend(message)
            }
            awaitClose()
        }
    }

    /**
     * Пользователь получает это уведомление, если другой пользователь отправил ему запрос в друзья,
     * подтвердил/отклонил его запрос в друзья или удалил его из друзей.
     */
    fun observeUpdateFriendship(): Flow<Message> {
        return callbackFlow {
            mainChannel.on(REQUEST_UPDATE_FRIENDSHIP) { message ->
                trySend(message)
            }
            awaitClose()
        }
    }

    /**
     * Add new users to group chat
     */
    fun pushAddUsers(payload: Map<String, Any>) =
        commonRequest(REQUEST_ADD_USERS, payload)

    /**
     * Remove user from chat
     */
    fun pushRemoveUser(payload: Map<String, Any>) = commonRequest(REQUEST_REMOVE_USER, payload)

    suspend fun pushRemoveUserSuspend(payload: Map<String, Any>) =
        suspendCoroutine<Message> { continuation ->
            mainChannel.push(REQUEST_REMOVE_USER, payload)
                .receive(STATUS_OK) { message ->
                    Timber.d("(Coroutine) Response after push (remove_chat_user): ${message.payload}")
                    continuation.resume(message)
                }
                .receive(STATUS_ERROR) { message ->
                    Timber.e("(Coroutine) ERROR response after push (remove_chat_user): ${message.payload}")
                    continuation.resume(message)
                }
        }


    /**
     * Add new admins to group chat
     */
    fun pushAddAdmins(payload: Map<String, Any>): Observable<Message> {
        return commonRequest(REQUEST_ADD_ADMINS, payload)
    }

    /**
     * Remove admin from group chat
     */
    fun pushRemoveAdmin(payload: Map<String, Any>) =
        commonRequest(REQUEST_REMOVE_ADMIN, payload)

    /**
     * Change group chat title
     */
    fun pushChangeTitle(payload: Map<String, Any>) =
        commonRequest(REQUEST_CHANGE_TITLE, payload)

    /**
     * Change group chat description
     */
    fun pushChangeDescription(payload: Map<String, Any>) =
        commonRequest(REQUEST_CHANGE_DESCRIPTION, payload)

    suspend fun pushCheckRoomSuspend(payload: Map<String, Any>): Message {
        return suspendCoroutine<Message> { continuation ->
            mainChannel.push(REQUEST_CHECK_ROOM, payload)
                .receive(STATUS_OK) { message ->
                    Timber.d("Response after push (check_room): ${message.payload}")
                    continuation.resume(message)
                }
                .receive(STATUS_ERROR) { message ->
                    Timber.e("ERROR response after push (check_room): ${message.payload}")
                    continuation.resume(message)
                }
        }
    }


    suspend fun pushMessageReadCoroutine(payload: Map<String, Any>) =
        suspendCoroutine<Message> { continuation ->
            mainChannel.push(REQUEST_MESSAGE_READ, payload)
                .receive(STATUS_OK) { message ->
                    Timber.d("(Coroutine) Response after push (message_read): ${message.payload}")
                    continuation.resume(message)
                }
                .receive(STATUS_ERROR) { message ->
                    Timber.e("(Coroutine) ERROR response after push (message_read): ${message.payload}")
                    continuation.resume(message)
                }
        }

    /**
     * Get friends list
     */
    fun pushGetFriends(payload: Map<String, Any>) =
        commonRequest(REQUEST_GET_FRIENDS, payload)

    suspend fun pushGetFriendsCoroutine(payload: Map<String, Any>) =
        withContext(Dispatchers.IO) {
            commonRequestSuspended(REQUEST_GET_FRIENDS, payload)
        }

    /**
     * Add friends request
     */
    fun pushAddFriends(payload: Map<String, Any>) =
        commonRequest(REQUEST_ADD_FRIENDS, payload)

    /**
     * Add friends request coroutine
     */
    suspend fun pushAddFriendsCoroutine(payload: Map<String, Any>) =
        suspendCoroutine<Message> { continuation ->
            mainChannel.push(REQUEST_ADD_FRIENDS, payload)
                .receive(STATUS_OK) { message ->
                    Timber.d("(Coroutine) Response after push ($REQUEST_ADD_FRIENDS): ${message.payload}")
                    continuation.resume(message)
                }
                .receive(STATUS_ERROR) { message ->
                    Timber.e("(Coroutine) ERROR response after push ($REQUEST_ADD_FRIENDS): ${message.payload}")
                    continuation.resume(message)
                }
        }

    /**
     * Remove from friends request
     */
    fun pushRemoveFriends(payload: Map<String, Any>) =
        commonRequest(REQUEST_REMOVE_FRIENDS, payload)

    /**
     * Get user profile, when success login
     */
    fun pushGetProfile(payload: Map<String, Any>): Single<Message> {
        return Single.create { singleEmitter ->
            mainChannel.push(REQUEST_GET_PROFILE, payload)
                .receive(STATUS_OK) { message ->
                    Timber.d("Response after push ($REQUEST_GET_PROFILE): ${message.payload}")
                    singleEmitter.onSuccess(message)
                }
                .receive(STATUS_ERROR) { message ->
                    Timber.e("ERROR response after push ($REQUEST_GET_PROFILE): ${message.payload}")
                    if (!singleEmitter.isDisposed) {
                        singleEmitter.onError(WebSocketResponseException(message.payload))
                    }
                }
        }
    }

    /**
     * Get user profile, when success login
     */
    suspend fun pushGetProfileSuspend(payload: Map<String, Any>) =
        suspendCoroutine<Message> { continuation ->
            mainChannel.push(REQUEST_GET_PROFILE, payload)
                .receive(STATUS_OK) { message ->
                    Timber.d("Response after push ($REQUEST_GET_PROFILE): ${message.payload}")
                    continuation.resume(message)
                }
                .receive(STATUS_ERROR) { message ->
                    Timber.e("ERROR response after push ($REQUEST_GET_PROFILE): ${message.payload}")
                    continuation.resumeWithException(IOException())
                }
        }

    /**
     * Get updated user info (list users)
     */
    suspend fun getUserInfo(payload: Map<String, Any>) =
        suspendCoroutine<Message> { continuation ->
            Timber.e("[WS] GET USER INFO: $payload")
            if (!socket.isConnected) {
                Timber.e("Socket DISCONNECTED (get_user_info)")
                continuation.resumeWithException(IOException())
            }

            mainChannel.push(REQUEST_GET_USER_INFO, payload)
                .receive(STATUS_OK) { message ->
                    Timber.d("(Coroutine) Response after push (get_user_info): ${message.payload}")
                    continuation.resume(message)
                }
                .receive(STATUS_ERROR) { message ->
                    Timber.e("(Coroutine) ERROR response after push (get_user_info): ${message.payload}")
                    continuation.resumeWithException(IOException())
                }
        }

    /**
     * Get user gifts
     */
    fun pushGetUserGifts(payload: Map<String, Any>) =
        commonRequest(REQUEST_GET_USER_GIFTS, payload)

    /**
     * Update user profile
     */
    fun pushUpdateUserProfile(payload: Map<String, Any>) =
        commonRequest(REQUEST_UPDATE_PROFILE, payload)

    suspend fun pushGetMapUserState(payload: Map<String, Any>) =
        commonRequestSuspended(REQUEST_MAP_USER_STATE, payload)


    suspend fun pushUpdateUserProfileSuspend(payload: Map<String, Any?>) =
        suspendCoroutine<Message> { continuation ->
            mainChannel.push(REQUEST_UPDATE_PROFILE, payload)
                .receive(STATUS_OK) { message ->
                    Timber.d("(Coroutine) Response after push (update_profile): ${message.payload}")
                    continuation.resume(message)
                }
                .receive(STATUS_ERROR) { message ->
                    Timber.e("(Coroutine) ERROR response after push (update_profile): ${message.payload}")
                    continuation.resume(message)
                }
        }

    suspend fun isUsernameUnique(payload: Map<String, Any?>): Message {
        return suspendCoroutine { continuation ->
            Timber.d("SOCKET REQUEST_IS_USERNAME_UNIQUE mainChannel.isClosed: ${mainChannel.isClosed}")
            Timber.d("SOCKET REQUEST_IS_USERNAME_UNIQUE mainChannel.canPush: ${mainChannel.canPush}")

            mainChannel.push(REQUEST_IS_USERNAME_UNIQUE, payload)
                .receive(STATUS_OK) { message ->
                    Timber.d("SOCKET REQUEST_IS_USERNAME_UNIQUE: ${message.payload}")
                    continuation.resume(message)
                }
                .receive(STATUS_ERROR) { message ->
                    Timber.d("SOCKET REQUEST_IS_USERNAME_UNIQUE: ${message.payload}")
                    continuation.resume(message)
                }
        }
    }

    /**
     * Get user photo albums
     */
    fun pushGetAlbums(payload: Map<String, Any>) = commonRequest(REQUEST_GET_ALBUM, payload)

    /**
     * Send code to userPhone
     */
    fun pushContactSendCode(payload: Map<String, Any>) = commonRequest(REQUEST_CONTACTS_SEND_CODE, payload)

    /**
     * Verify user phone
     */
    fun pushContactVerify(payload: HashMap<String, Any>) = commonRequest(REQUEST_CONTACT_VERIFY, payload)

    fun pushContactSync(payload: Map<String, Any>) = commonRequest(REQUEST_CONTACTS_SYNC, payload)

    /**
     * Is user phone verified
     */
    fun pushIsUserVerifyed(payload: Map<String, Any>) = commonRequest(REQUEST_CONTACT_CHECK, payload)

    /**
     * get profile privacy settings
     */
    fun pushGetPrivacySettings(payload: Map<String, Any>) = commonRequest(REQUEST_GET_PRIVACY_SETTINGS, payload)

    /**
     * set profile privacy settings
     */
    fun pushSetPrivacySettings(payload: Map<String, Any>) = commonRequest(REQUEST_SET_PRIVACY_SETTINGS, payload)

    suspend fun pushSetPrivacySettingsSuspended(payload: Map<String, Any>) = commonRequestSuspended(REQUEST_SET_PRIVACY_SETTINGS, payload)

    /**
     * return wizard for vehicle
     */
    fun pushGetVehicleInfo(payload: Map<String, Any>) = commonRequest(REQUEST_VEHICLE_INFO, payload)

    /**
     * get vehicle brands by type
     */
    fun pushGetVehicleBrandByType(payload: Map<String, Any>) = commonRequest(REQUEST_GET_BRANDS_BY_TYPE, payload)

    /**
     * get vehicle models by brand
     */
    fun pushGetVehicleModelsByBrand(payload: Map<String, Any>) = commonRequest(REQUEST_GET_MODELS_BY_BRAND, payload)

    /**
     * get vehicle types
     */
    fun pushGetVehiclesTypes(payload: Map<String, Any>) = commonRequest(REQUEST_GET_VEHICLE_TYPES, payload)

    /**
     * Test disconnect request
     */
    fun pushGetDisconnect(payload: Map<String, Any>) =
        commonRequest(REQUEST_TEST_DISCONNECT, payload)

    /**
     * Setup user call privacy
     * */
    fun pushSetCallPrivacyForUset(payload: Map<String, Any>) =
        commonRequest(REQUEST_SET_CALL_PRIVACY_FOR_USER, payload)

    suspend fun pushSetCallPrivacyForUserSuspend(payload: Map<String, Any?>) =
        suspendCoroutine<Message> { continuation ->
            mainChannel.push(REQUEST_SET_CALL_PRIVACY_FOR_USER, payload)
                .receive(STATUS_OK) { message ->
                    Timber.d("(Coroutine) Response after push (set_call_privacy_for_user): ${message.payload}")
                    continuation.resume(message)
                }
                .receive(STATUS_ERROR) { message ->
                    Timber.e("(Coroutine) ERROR response after push (set_call_privacy_for_user): ${message.payload}")
                    continuation.resume(message)
                }
        }

    /**
     * This method needs to be called when call started as params used id and roomID
     * */
    fun pushCallStarted(payload: Map<String, Any>) = mainChannel.push(REQUEST_CALL_STARTED, payload)

    /**
     * This method needs to be called when call finished as params used id and roomID
     * */
    fun pushCallFinished(payload: Map<String, Any>) = mainChannel.push(REQUEST_CALL_FINISHED, payload)

    fun pushSetMessageNotificationPrivacy(payload: Map<String, Any>) =
        commonRequest(REQUEST_NOTIFICATION_PRIVACY, payload)

    @Deprecated("Use REST (/v2/users/{user_id}/permissions/message_notification)")
    suspend fun pushMessageNotificationPrivacySuspend(payload: Map<String, Any?>) =
        suspendCoroutine<Message> { continuation ->
            mainChannel.push(REQUEST_NOTIFICATION_PRIVACY, payload)
                .receive(STATUS_OK) { message ->
                    Timber.d("(Coroutine) Response after push (set_message_notification_privacy): ${message.payload}")
                    continuation.resume(message)
                }
                .receive(STATUS_ERROR) { message ->
                    Timber.e("(Coroutine) ERROR response after push (set_message_notification_privacy): ${message.payload}")
                    continuation.resume(message)
                }
        }

    /**
     * Mute chat room
     */
    fun muteRoom(payload: Map<String, Any>) = commonRequest(REQUEST_MUTE_ROOM, payload)

    suspend fun muteRoomSuspend(payload: Map<String, Any?>) =
        suspendCoroutine<Message> { continuation ->
            mainChannel.push(REQUEST_MUTE_ROOM, payload)
                .receive(STATUS_OK) { message ->
                    Timber.d("(Coroutine) Response after push (mute_room): ${message.payload}")
                    continuation.resume(message)
                }
                .receive(STATUS_ERROR) { message ->
                    Timber.e("(Coroutine) ERROR response after push (mute_room): ${message.payload}")
                    continuation.resume(message)
                }
        }

    /**
     * Unmute chat room
     */
    fun unmuteRoom(payload: Map<String, Any>) = commonRequest(REQUEST_UNMUTE_ROOM, payload)

    suspend fun unmuteRoomSuspend(payload: Map<String, Any?>) =
        suspendCoroutine<Message> { continuation ->
            mainChannel.push(REQUEST_UNMUTE_ROOM, payload)
                .receive(STATUS_OK) { message ->
                    Timber.d("(Coroutine) Response after push (unmute_room): ${message.payload}")
                    continuation.resume(message)
                }
                .receive(STATUS_ERROR) { message ->
                    Timber.e("(Coroutine) ERROR response after push (unmute_room): ${message.payload}")
                    continuation.resume(message)
                }
        }

    /**
     * Common method for all push
     */
    private fun commonRequest(method: String, payload: Map<String, Any>): Observable<Message> {
        Timber.d("START $method")
        return Observable.create { emitter ->
            mainChannel.push(method, payload)
                .receive(STATUS_OK) { message ->
                    Timber.d("Response after push ($method): ${message.payload}")
                    if (!emitter.isDisposed) {
                        emitter.onNext(message)
                    }
                }
                .receive(STATUS_ERROR) { message ->
                    Timber.e("ERROR response after push ($method): ${message.payload}")
                    if (!emitter.isDisposed) {
                        emitter.onError(WebSocketResponseException(message.payload))
                    }
                    if (::activityInteractionCallback.isInitialized)
                        activityInteractionCallback.onErrorHandler(message.payload)
                }
            if (!socket.isConnected) {
                if (!emitter.isDisposed) {
                    emitter.onError(WebSocketResponseException("(Common request) Socket disconnected"))
                }
            }
        }
    }

    private suspend fun commonRequestSuspended(method: String, payload: Map<String, Any>): Message {
        Timber.d("START $method")
        return suspendCancellableCoroutine { continuation ->
            mainChannel.push(method, payload)
                .receive(STATUS_OK) { message ->
                    if (continuation.isActive) {
                        Timber.d("Response after push ($method): ${message.payload}")
                        continuation.resume(message)
                    }
                }
                .receive(STATUS_ERROR) { message ->
                    Timber.e("ERROR response after push ($method): ${message.payload}")
                    if (::activityInteractionCallback.isInitialized)
                        activityInteractionCallback.onErrorHandler(message.payload)

                    if (continuation.isActive) {
                        continuation.resumeWithException(WebSocketResponseException(message.payload))
                    }
                }

            if (socket.isConnected.not()) {
                if (continuation.isActive) {
                    continuation.resumeWithException(WebSocketResponseException("(Common request) Socket disconnected"))
                }
            }
        }
    }

    private fun setupStartListeners() {
        mainChannel.on(SUBSCRIPTION_NEW_POST_UPDATE) { message ->
            subscriptionNewPostSubject.onNext(message)
        }
        mainChannel.on(SUBSCRIPTION_NEW_MOMENTS_UPDATE) { message ->
            subscriptionNewMomentSubject.onNext(message)
        }
    }

    private fun setupLogger(socket: Socket?) {
        socket?.logger = { message ->
            if (!message.contains("online")) {
                Timber.d("Socket LOG: $message")
            }
        }
        socket?.onOpen {
            Timber.d("Socket Opened")
            publishSocketConnection.onNext(true)
        }
        socket?.onClose {
            Timber.d("Socket Closed")
            publishSocketConnection.onNext(false)
        }
        socket?.onError { throwable, response -> handleSocketError(throwable, response) }
    }

    private fun handleSocketError(throwable: Throwable, response: Response?) {
        Timber.e("Socket Error: !!!Throwable: $throwable !!!code: ${response?.code} !!!message: ${response?.message} ")
        if (response?.code == HTTP_CODE_FORBIDDEN || response?.code == HTTP_CODE_NOT_AUTHORIZED)
            if (::activityInteractionCallback.isInitialized)
                activityInteractionCallback.onSocketPhxError(response)
        throwable.printStackTrace()
    }

    fun observeSignallingChannel(): Observable<Message> {
        Timber.d("Start Observe Signalling Channel")
        return Observable.create { emitter ->
            mainChannel.on(REQUEST_SIGNALING) { message ->
                Timber.d("Observe SIGNALING: ${message.payload}")
                emitter.onNext(message)
            }
        }
    }

    fun observeSignallingFlow(): Flow<Message> {
        return callbackFlow<Message> {
            mainChannel.on(REQUEST_SIGNALING) { message ->
                trySend(message)
            }
            awaitClose()
        }
    }

    fun pushSignalingMessage(method: String, payload: Map<String, Any>): Observable<Message> {
        return Observable.create { emitter ->
            mainChannel.push(/*REQUEST_STARTCALL*/method, payload)
                .receive(STATUS_OK) { message ->
                    Timber.d("Response after push ($method): ${message.payload}")
                    emitter.onNext(message)
                }
                .receive(STATUS_ERROR) { message ->
                    Timber.e("ERROR response after push ($method): ${message.payload}")
                    // TODO: Handle error
                }
        }
    }

    suspend fun pushSignalingMessageSuspend(
        method: String,
        payload: Map<String, Any?>
    ) = withContext(Dispatchers.IO) {
        suspendCoroutine<Message> { continuation ->
            mainChannel.push(method, payload)
                .receive(STATUS_OK) { message ->
                    Timber.d("Response after push ($method): ${message.payload}")
                    continuation.resume(message)
                }
                .receive(STATUS_ERROR) { message ->
                    Timber.e("ERROR response after push ($method): ${message.payload}")
                    continuation.resumeWithException(
                        IOException("ERROR Push signalling message! Method:($method) Message:$message")
                    )
                }
        }
    }



}
