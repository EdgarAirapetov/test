package com.numplates.nomera3.data.fcm

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import com.meera.core.extensions.empty
import com.meera.core.extensions.simpleName
import com.meera.core.preferences.AppSettings
import com.meera.db.models.dialog.UserChat
import com.numplates.nomera3.ACTION_INCOMING_CALL
import com.numplates.nomera3.App
import com.numplates.nomera3.CALL_ACTION_OPEN_CALL
import com.numplates.nomera3.TYPE_CALL_ACTION
import com.numplates.nomera3.data.fcm.IPushInfo.ADD_TO_GROUP_CHAT
import com.numplates.nomera3.data.fcm.IPushInfo.CHAT_INCOMING_MESSAGE
import com.numplates.nomera3.data.fcm.IPushInfo.CHAT_REQUEST
import com.numplates.nomera3.data.fcm.IPushInfo.COMMENT_REACTION
import com.numplates.nomera3.data.fcm.IPushInfo.EVENT_CALL_UNAVAILABLE
import com.numplates.nomera3.data.fcm.IPushInfo.EVENT_START_SOON
import com.numplates.nomera3.data.fcm.IPushInfo.FRIEND_CONFIRM
import com.numplates.nomera3.data.fcm.IPushInfo.FRIEND_REQUEST
import com.numplates.nomera3.data.fcm.IPushInfo.GALLERY_REACTION
import com.numplates.nomera3.data.fcm.IPushInfo.GIFT_RECEIVED_PUSH
import com.numplates.nomera3.data.fcm.IPushInfo.HOLIDAY_DAILY_VISITS
import com.numplates.nomera3.data.fcm.IPushInfo.MOMENT
import com.numplates.nomera3.data.fcm.IPushInfo.MOMENT_COMMENT
import com.numplates.nomera3.data.fcm.IPushInfo.MOMENT_COMMENT_REACTION
import com.numplates.nomera3.data.fcm.IPushInfo.MOMENT_COMMENT_REPLY
import com.numplates.nomera3.data.fcm.IPushInfo.MOMENT_MENTION_COMMENT
import com.numplates.nomera3.data.fcm.IPushInfo.MOMENT_REACTION
import com.numplates.nomera3.data.fcm.IPushInfo.PEOPLE
import com.numplates.nomera3.data.fcm.IPushInfo.POST_COMMENTS_PUSH
import com.numplates.nomera3.data.fcm.IPushInfo.POST_REACTION
import com.numplates.nomera3.data.fcm.IPushInfo.PUSH_BACK_TO_APP
import com.numplates.nomera3.data.fcm.IPushInfo.PUSH_BIRTHDAY
import com.numplates.nomera3.data.fcm.IPushInfo.PUSH_BIRTHDAY_GROUP
import com.numplates.nomera3.data.fcm.IPushInfo.PUSH_CALL_START
import com.numplates.nomera3.data.fcm.IPushInfo.PUSH_GROUP_REQUEST
import com.numplates.nomera3.data.fcm.IPushInfo.PUSH_MENTION_GROUP_CHAT
import com.numplates.nomera3.data.fcm.IPushInfo.PUSH_OPEN_CHAT
import com.numplates.nomera3.data.fcm.IPushInfo.PUSH_OPEN_EVENTS
import com.numplates.nomera3.data.fcm.IPushInfo.PUSH_OPEN_MAP
import com.numplates.nomera3.data.fcm.IPushInfo.PUSH_OPEN_PROFILE
import com.numplates.nomera3.data.fcm.IPushInfo.PUSH_OPEN_REFERAL
import com.numplates.nomera3.data.fcm.IPushInfo.PUSH_OPEN_ROAD
import com.numplates.nomera3.data.fcm.IPushInfo.PUSH_SELF_BIRTHDAY
import com.numplates.nomera3.data.fcm.IPushInfo.SUBSCRIBERS_AVATAR_POST_CREATE
import com.numplates.nomera3.data.fcm.IPushInfo.SUBSCRIBERS_POST_CREATE
import com.numplates.nomera3.data.fcm.IPushInfo.SYSTEM_NOTIFICATION
import com.numplates.nomera3.data.fcm.models.PushCallObject
import com.numplates.nomera3.domain.interactornew.DeliveredUseCase
import com.numplates.nomera3.modules.analytics.domain.AnalyticsInteractor
import com.numplates.nomera3.modules.baseCore.helper.notification.NotificationManagerImpl.Companion.COMMENTS_BASE
import com.numplates.nomera3.modules.baseCore.helper.notification.NotificationManagerImpl.Companion.FRIENDS_BASE
import com.numplates.nomera3.modules.baseCore.helper.notification.NotificationManagerImpl.Companion.GIFTS_BASE
import com.numplates.nomera3.modules.baseCore.helper.notification.NotificationManagerImpl.Companion.GROUP_REQUEST_BASE
import com.numplates.nomera3.modules.baseCore.helper.notification.NotificationManagerImpl.Companion.MESSENGER_BASE
import com.numplates.nomera3.modules.baseCore.helper.notification.NotificationManagerImpl.Companion.NOOMEERA_BASE
import com.numplates.nomera3.modules.registration.ui.FirebasePushSubscriberDelegate
import com.numplates.nomera3.presentation.router.IActionContainer
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_MESSAGE_ID
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_PUSH_OBJECT
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_ROOM_ID
import com.numplates.nomera3.presentation.router.IArgContainer.ARG_USER_MODEL
import com.numplates.nomera3.telecom.CallNotificationManager
import com.numplates.nomera3.telecom.MeeraSignalingService
import com.numplates.nomera3.telecom.MeeraSignalingServiceBinder
import com.yandex.metrica.push.firebase.MetricaMessagingService
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject


class FirebaseIDService : FirebaseMessagingService(), IPushInfo, IActionContainer {

    private val disposable = CompositeDisposable()

    @Inject
    lateinit var deliveredUseCase: DeliveredUseCase

    @Inject
    lateinit var notificationHelper: NotificationHelper

    @Inject
    lateinit var appSettings: AppSettings

    @Inject
    lateinit var firebasePushSubscriberDelegate: FirebasePushSubscriberDelegate

    @Inject
    lateinit var app: App

    @Inject
    lateinit var gson: Gson

    @Inject
    lateinit var callNotificationManager: CallNotificationManager

    // TODO: удалить после тестирования https://nomera.atlassian.net/browse/BR-21050
    @Inject
    lateinit var amplitudeHelper: AnalyticsInteractor

    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    init {
        App.component.inject(this)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        amplitudeHelper.logFirebaseOnNewToken(token)
        Timber.e("NEW_TOKEN: $token")
        MetricaMessagingService().processToken(this, token)
        firebasePushSubscriberDelegate.subscribePush(token)
        appSettings.writeFCMToken(token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        amplitudeHelper.logFirebaseGetMessage(remoteMessage.data.toString())
        Timber.e("REMOTE MESSAGE (Notification): ${Gson().toJson(remoteMessage.notification?.body)}")
        Timber.e("REMOTE MESSAGE (Data): ${Gson().toJson(remoteMessage.data)}   ${remoteMessage.data}")

        if (!appSettings.readAccessToken().isNullOrEmpty()) {
            handleMessageNew(remoteMessage)
        }
    }

    /** Appmetrica Push SDK should recognize and show their push automatically */
    private fun showPushIfAppmetrica(remoteMessage: RemoteMessage) =
        MetricaMessagingService().processPush(this, remoteMessage)

    private fun handleMessageNew(msg: RemoteMessage) {
        showPushIfAppmetrica(msg)
        Timber.e("PUSH Received")

        val content = gson.toJson(msg.data)

        Timber.e("PUSH_MESSAGE $content")

        val pushObject = parsePushNew(content ?: String.empty())
        val pushType = pushObject?.type

        if (pushObject?.type == null || pushObject.type.isEmpty()) {
            Timber.e("empty PUSH")
            return
        }
        when (pushType) {
            MOMENT_COMMENT_REACTION -> {
                notificationHelper.show(IActionContainer.ACTION_OPEN_MOMENT, NOOMEERA_BASE, pushObject)
            }

            MOMENT_MENTION_COMMENT -> {
                notificationHelper.show(IActionContainer.ACTION_OPEN_MOMENT, NOOMEERA_BASE, pushObject)
            }

            MOMENT_COMMENT_REPLY -> {
                notificationHelper.show(IActionContainer.ACTION_OPEN_MOMENT, NOOMEERA_BASE, pushObject)
            }

            MOMENT_REACTION -> {
                notificationHelper.show(IActionContainer.ACTION_OPEN_MOMENT, NOOMEERA_BASE, pushObject)
            }

            MOMENT_COMMENT -> {
                notificationHelper.show(IActionContainer.ACTION_OPEN_MOMENT, NOOMEERA_BASE, pushObject)
            }

            MOMENT -> {
                notificationHelper.show(IActionContainer.ACTION_OPEN_MOMENT, NOOMEERA_BASE, pushObject)
            }

            COMMENT_REACTION -> {
                notificationHelper.show(IActionContainer.ACTION_LEAVE_POST_COMMENT_REACTIONS, NOOMEERA_BASE, pushObject)
            }

            POST_REACTION -> {
                notificationHelper.show(IActionContainer.ACTION_OPEN_POST_WITH_REACTIONS, NOOMEERA_BASE, pushObject)
            }

            GALLERY_REACTION -> {
                notificationHelper.show(IActionContainer.ACTION_OPEN_GALLERY_WITH_REACTIONS, NOOMEERA_BASE, pushObject)
            }

            PEOPLE -> {
                notificationHelper.show(IActionContainer.ACTION_OPEN_PEOPLE, NOOMEERA_BASE, pushObject)
            }
            // VoIP
            PUSH_CALL_START -> /*if (isPushOn)*/ {
                runCatching {
                    val data = gson.fromJson(content, PushCallObject::class.java)
                    showCallNotification(data)
                }.onFailure {
                    Timber.d("CALL_LOG incoming error when start Signalling service:$it")
                    it.printStackTrace()
                }
            }

            CHAT_INCOMING_MESSAGE,

            CHAT_REQUEST -> {
                if (app.hashSetRooms.contains(pushObject.roomId)) return
                notificationHelper.show(IActionContainer.ACTION_OPEN_CHAT, MESSENGER_BASE, pushObject)
                isSystemNotificationsEnabled { markMessageAsDelivered(pushObject) }
            }

            FRIEND_REQUEST -> {
                sendEventCounter()
                notificationHelper.show(IActionContainer.ACTION_FRIEND_REQUEST, FRIENDS_BASE, pushObject)
            }

            FRIEND_CONFIRM -> {
                sendEventCounter()
                notificationHelper.show(IActionContainer.ACTION_FRIEND_CONFIRM, FRIENDS_BASE, pushObject)
            }

            ADD_TO_GROUP_CHAT -> {
                sendEventCounter()
                notificationHelper.show(IActionContainer.ACTION_OPEN_CHAT, MESSENGER_BASE, pushObject)
            }

            GIFT_RECEIVED_PUSH -> {
                sendEventCounter()
                notificationHelper.show(IActionContainer.ACTION_OPEN_GIFTS, GIFTS_BASE, pushObject)
            }

            POST_COMMENTS_PUSH -> {
                sendEventCounter()
                notificationHelper.show(IActionContainer.ACTION_LEAVE_POST_COMMENTS, COMMENTS_BASE, pushObject)
            }

            PUSH_GROUP_REQUEST -> {
                sendEventCounter()
                notificationHelper.show(IActionContainer.ACTION_REQUEST_TO_GROUP, GROUP_REQUEST_BASE, pushObject)
            }

            PUSH_BACK_TO_APP -> {
                notificationHelper.show(IActionContainer.ACTION_OPEN_APP, NOOMEERA_BASE, pushObject)
            }

            PUSH_OPEN_MAP -> {
                notificationHelper.show(IActionContainer.ACTION_OPEN_MAP, NOOMEERA_BASE, pushObject)
            }

            SUBSCRIBERS_POST_CREATE -> {
                notificationHelper.show(IActionContainer.ACTION_OPEN_POST, NOOMEERA_BASE, pushObject)
            }

            SUBSCRIBERS_AVATAR_POST_CREATE -> {
                notificationHelper.show(IActionContainer.ACTION_OPEN_GALLERY_WITH_REACTIONS, NOOMEERA_BASE, pushObject)
            }

            PUSH_OPEN_EVENTS -> {
                notificationHelper.show(IActionContainer.ACTION_OPEN_EVENT, NOOMEERA_BASE, pushObject)
            }

            PUSH_OPEN_ROAD -> {
                notificationHelper.show(IActionContainer.ACTION_OPEN_APP, NOOMEERA_BASE, pushObject)
            }

            PUSH_OPEN_PROFILE -> {
                notificationHelper.show(IActionContainer.ACTION_OPEN_OWN_PROFILE, NOOMEERA_BASE, pushObject)
            }

            PUSH_OPEN_CHAT -> {
                notificationHelper.show(IActionContainer.ACTION_OPEN_OWN_CHAT_LIST, NOOMEERA_BASE, pushObject)
            }

            PUSH_OPEN_REFERAL -> {
                notificationHelper.show(IActionContainer.ACTION_OPEN_REFERAL, NOOMEERA_BASE, pushObject)
            }

            PUSH_BIRTHDAY -> {
                notificationHelper.show(IActionContainer.ACTION_OPEN_BIRTHDAY_GIFTS, NOOMEERA_BASE, pushObject)
            }

            PUSH_BIRTHDAY_GROUP -> {
                notificationHelper.show(IActionContainer.ACTION_OPEN_BIRTHDAY_GROUP, NOOMEERA_BASE, pushObject)
            }

            SYSTEM_NOTIFICATION -> {
                notificationHelper.show(IActionContainer.ACTION_SYSTEM_EVENT, NOOMEERA_BASE, pushObject)
            }

            PUSH_MENTION_GROUP_CHAT -> {
                notificationHelper.show(IActionContainer.ACTION_OPEN_CHAT, MESSENGER_BASE, pushObject)
            }

            HOLIDAY_DAILY_VISITS -> {
                notificationHelper.show(IActionContainer.ACTION_OPEN_APP, NOOMEERA_BASE, pushObject)
            }

            PUSH_SELF_BIRTHDAY -> {
                notificationHelper.show(
                    action = IActionContainer.ACTION_OPEN_SELF_BIRTHDAY,
                    channelIdBase = NOOMEERA_BASE,
                    data = pushObject
                )
            }

            EVENT_START_SOON -> {
                notificationHelper.show(
                    action = IActionContainer.ACTION_OPEN_EVENT_ON_MAP,
                    channelIdBase = NOOMEERA_BASE,
                    data = pushObject
                )
            }

            EVENT_CALL_UNAVAILABLE -> {
                notificationHelper.show(
                    action = IActionContainer.ACTION_CALL_UNAVAILABLE,
                    channelIdBase = NOOMEERA_BASE,
                    data = pushObject
                )
            }
        }
    }

    private fun markMessageAsDelivered(pushObject: PushObjectNew) {
        val d = deliveredUseCase.markMessageAsDelivered(pushObject.roomId ?: 0, arrayListOf(pushObject.messageId ?: ""))
            .subscribeOn(Schedulers.io())
            .subscribe({
                Timber.d("success send as delivered")
            }, {
                Timber.e(it)
            })
        disposable.add(d)
    }

    private fun isSystemNotificationsEnabled(block: () -> Unit) {
        val isNotificationsEnabled = NotificationManagerCompat
            .from(app.applicationContext)
            .areNotificationsEnabled()
        if (isNotificationsEnabled) block()
    }

    private fun sendEventCounter() {
        coroutineScope.launch {
            Timber.d("Bazaleev sendEventCounter")
            val value = appSettings.newEvent.get() ?: false
            appSettings.newEvent.set(!value)
        }
    }

    private fun showCallNotification(pushObject: PushCallObject) {
        val callUser = UserChat(
            userId = pushObject.userId ?: 0,
            name = pushObject.name,
            avatarSmall = pushObject.avatarBig
        )
        if (pushObject.startCall?.not() == true) {
            callNotificationManager.cancelCallNotification()
        } else {
            callNotificationManager.cancelCallNotification()
            callNotificationManager.createPushForIncomingCall(
                userChat = callUser,
                callPushObject = pushObject
            )
        }
    }

    /**
     * С новых версий андроид запрещено запускать сервисы,
     * когда приложение выгружено
     */
    @Suppress("detekt:UnusedPrivateMember")
    @Deprecated("Unused")
    private fun startSignalingService(pushObject: PushCallObject) {
        if (pushObject.startCall == true) {
            val userChat = UserChat(
                userId = pushObject.userId ?: 0,
                name = pushObject.name,
                avatarSmall = pushObject.avatarBig
            )
            val signalingIntent = Intent(applicationContext, MeeraSignalingService::class.java)
                .setAction(ACTION_INCOMING_CALL)
                .addFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION or Intent.FLAG_ACTIVITY_NEW_TASK)
                .putExtra(ARG_ROOM_ID, pushObject.roomId?.toLong())
                .putExtra(ARG_MESSAGE_ID, pushObject.messageId)
                .putExtra(TYPE_CALL_ACTION, CALL_ACTION_OPEN_CALL)
                .putExtra(ARG_USER_MODEL, userChat)
                .putExtra(ARG_PUSH_OBJECT, gson.toJson(pushObject))
            startForegroundService(signalingIntent)
        } else {

            val serviceConnection: ServiceConnection = object : ServiceConnection {
                override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                    (service as MeeraSignalingServiceBinder).getService()?.get()?.cancelCallNotification()
                }

                override fun onServiceDisconnected(name: ComponentName?) {
                    Timber.d("${MeeraSignalingServiceBinder::simpleName} disconnected!")
                }
            }
            bindService(
                Intent(this, MeeraSignalingServiceBinder::class.java),
                serviceConnection,
                Context.BIND_AUTO_CREATE
            )
        }
    }

    private fun parsePushNew(content: String): PushObjectNew? {
        val pushObject: PushObjectNew
        return try {
            pushObject = gson.fromJson(content, PushObjectNew::class.java)
            pushObject
        } catch (e: Exception) {
            e.printStackTrace()
            Timber.e("failed to parse PUSH")
            null
        }
    }

    override fun onDestroy() {
        Timber.d("ON_DESTROY FIREBASE SERVICE")
        super.onDestroy()
    }
}
