package com.numplates.nomera3.modules.notifications.service

import androidx.annotation.WorkerThread
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.meera.core.di.scopes.AppScope
import com.meera.core.extensions.empty
import com.meera.core.extensions.fromJson
import com.meera.core.network.websocket.WebSocketMainChannel
import com.meera.core.preferences.AppSettings
import com.meera.db.DataStore
import com.numplates.nomera3.data.newmessenger.response.ResponseWrapperWebSock
import com.numplates.nomera3.modules.notifications.data.repository.NotificationRepository
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import org.phoenixframework.Payload
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

private const val LIMIT = 20
private const val OFFSET = 0

private const val DEBOUNCE_MILLISECOND = 500L
private const val SOCKET_EVENTS_DELAY = 500L

interface SyncNotificationService {
    fun startListening()

    fun stopListening()

    fun fetchNotifications(
        subscribeUpdates: Boolean = true,
        withEvents: Boolean = false,
        limit: Int = LIMIT,
        offset: Int = OFFSET,
        createdAt: Long? = null
    )

    fun addRefreshListener(action: (Long) -> Unit)

    fun removeRefreshListener(action: (Long) -> Unit)

    fun unreadNotificationCountObservable(): Observable<Int>

    fun unreadNotificationCountFlowObservable(): Flow<Boolean>

    fun setEventListener(action: (NotificationActionEntity) -> Unit)
}

@AppScope
class SyncNotificationServiceImpl @Inject constructor(
    private val webSocket: WebSocketMainChannel,
    private val ds: DataStore,
    private val gson: Gson,
    private val repo: NotificationRepository,
    private val appSettings: AppSettings
) : SyncNotificationService {

    private val refreshListListener = mutableListOf<(Long) -> Unit>()

    private var newEventListener: (NotificationActionEntity) -> Unit = {}

    private var isFirstConnection = true

    private val source = PublishSubject.create<Boolean>()

    private val disposable: CompositeDisposable = CompositeDisposable()

    override fun unreadNotificationCountObservable(): Observable<Int> =
        source.debounce(DEBOUNCE_MILLISECOND, TimeUnit.MILLISECONDS)
            .switchMap { fetchUnreadNotificationCount() }

    override fun unreadNotificationCountFlowObservable(): Flow<Boolean> {
        return webSocket.observeNotificationsUpdate().map { _ ->
            return@map true
        }.flowOn(Dispatchers.IO)
    }

    override fun fetchNotifications(
        subscribeUpdates: Boolean,
        withEvents: Boolean,
        limit: Int,
        offset: Int,
        createdAt: Long?,
    ) {
        Timber.d("Bazaleev: sync_events_update fetchNotifications")
        webSocket.pushEventsUpdates(createdAt = createdAt)
            .map {
                val a =
                    gson.fromJson<ResponseWrapperWebSock<NotificationSocketResponse>>(gson.toJson(it.payload))
                a.response!!
            }
            .subscribeOn(Schedulers.io())
            .subscribeBy(
                onSuccess = {
                    if (isFirstConnection) {
                        isFirstConnection = false
                    }
                    Timber.d("sync_events_update success response = $it")
                },
                onError = {
                    Timber.d("sync_events_update: SyncNotificationService onError-$it")
                }
            )
            .addTo(disposable)
    }

    override fun startListening() {
        val isChannelJoined = webSocket.isChannelJoined()
        val isSockedConnected = webSocket.isConnected()
        //if socket connected we can request data if not
        //we should first wait till connected status
        if (isChannelJoined && isSockedConnected) {
            observeReactiveEvents()
            fetchNotifications(createdAt = appSettings.lastNotificationTs)
        } else {
            webSocket.publishSocketConnection
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .filter { it && webSocket.isConnected() }
                .take(1)
                .subscribe({ isConnected ->
                    if (isConnected) {
                        observeReactiveEvents()
                        fetchNotifications(createdAt = appSettings.lastNotificationTs)
                    }
                }, {
                    Timber.e(it)
                }).addTo(disposable)
        }
    }

    private fun observeReactiveEvents() {
        webSocket.observableReactiveEventsUpdates()
            .map { it.payload.makeEntity<NotificationActionEntity>() }
            .subscribeOn(Schedulers.io())
            .delay(SOCKET_EVENTS_DELAY, TimeUnit.MILLISECONDS)
            .subscribeBy(
                onNext = { handleReactiveCommand(it) },
                onError = {
                    Timber.d("Fetisov: SyncNotificationService onError-$it")
                },
                onComplete = {
                    Timber.d("Fetisov: SyncNotificationService onComplete")
                }
            )
            .addTo(disposable)
    }

    override fun addRefreshListener(action: (Long) -> Unit) {
        refreshListListener.add(action)
    }

    override fun removeRefreshListener(action: (Long) -> Unit) {
        refreshListListener.remove(action)
    }

    override fun setEventListener(action: (NotificationActionEntity) -> Unit) {
        this.newEventListener = action
    }

    override fun stopListening() {
        disposable.clear()
    }

    @WorkerThread
    private fun handleReactiveCommand(entity: NotificationActionEntity) {
        Timber.d("sync_events_update: SyncNotificationService handleReactiveCommand-$entity")
        appSettings.lastNotificationTs = entity.ts

        source.onNext(true)

        handleActionList(mutableListOf(entity))
    }

    private fun handleActionList(actions: List<NotificationActionEntity>) {
        actions.forEach { entity ->
            when (NotificationActionType.from(entity.action)) {
                NotificationActionType.ADD -> { /* do nothing */
                    addNotify(entity)
                }

                NotificationActionType.READ ->
                    ds.notificationDao().updateIsReadById(true, entity.id)

                NotificationActionType.DELETE -> {
                    ds.notificationDao().deleteById(entity.id)
                }


                NotificationActionType.REFRESH -> {
                    refreshNotify(entity.ts)
                }

            }
        }
    }

    private fun addNotify(entity: NotificationActionEntity) {
        newEventListener(entity)
    }

    private fun refreshNotify(ts: Long) {
        try {
            refreshListListener.forEach {
                it(ts)
            }
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    private fun fetchUnreadNotificationCount(): Observable<Int> =
        repo.getUnreadNotificationCount().toObservable()

    private inline fun <reified T> Payload.makeEntity() =
        this.makeJson().makeEntity<T>()

    private inline fun <reified T> String.makeEntity() =
        gson.fromJson<T>(this, object : TypeToken<T>() {}.type)

    private fun Payload.makeJson(): String = gson.toJson(this) ?: String.empty()
}
