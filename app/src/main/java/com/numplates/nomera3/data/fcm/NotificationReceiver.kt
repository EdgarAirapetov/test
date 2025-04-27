package com.numplates.nomera3.data.fcm

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.RemoteInput
import com.meera.db.models.dialog.UserChat
import com.numplates.nomera3.App
import com.numplates.nomera3.R
import com.numplates.nomera3.domain.interactornew.GetUserUidUseCase
import com.numplates.nomera3.domain.interactornew.SendNewMessageUseCase
import com.numplates.nomera3.modules.analytics.domain.AnalyticsInteractor
import com.numplates.nomera3.modules.chat.notification.ChatPushData
import com.numplates.nomera3.modules.chat.notification.MessageStyleNotificationCreator
import com.numplates.nomera3.modules.userprofile.domain.usecase.GetOwnLocalProfileUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

const val DELAY_DELETE_NOTIFICATION = 1000

class NotificationReceiver : BroadcastReceiver() {

    init {
        App.component.inject(this)
    }

    @Inject
    lateinit var userUidUseCase: GetUserUidUseCase

    @Inject
    lateinit var getOwnLocalProfileUseCase: GetOwnLocalProfileUseCase

    @Inject
    lateinit var amplitudeHelper: AnalyticsInteractor

    @Inject
    lateinit var newMessageUseCase: SendNewMessageUseCase

    @Inject
    lateinit var messageStyleNotificationCreator: MessageStyleNotificationCreator

    private var pendingResult: PendingResult? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())


    override fun onReceive(context: Context, intent: Intent) {
        val remoteInput = RemoteInput.getResultsFromIntent(intent)
        val message = remoteInput?.getCharSequence(NotificationHelper.KEY_TEXT_REPLY)?.toString()
        val pushId = intent.getIntExtra(NotificationHelper.KEY_PUSH_ID, 0)
        val roomId = intent.getLongExtra(NotificationHelper.KEY_ROOM_ID, 0L)
        val senderId = intent.getLongExtra(NotificationHelper.KEY_SENDER_ID, 0L)
        logPushAnswerTap(senderId)
        if (!message.isNullOrBlank() && roomId != 0L) {
            val nm = NotificationManagerCompat.from(context)
            val payload = hashMapOf<String, Any>(
                "content" to message,
                "room_id" to roomId,
                "type" to "dialog"
            )
            pendingResult = goAsync()
            scope.launch {
                try {
                    val response = newMessageUseCase.newMessage(payload)
                    response.data ?: throw error("response data = null smt. went wrong")
                    handleResponse(response.data, pushId, context, message)
                } catch (e: Exception) {
                    val notification = getActiveNotification(pushId, context)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        Notification.Builder
                            .recoverBuilder(context, notification)
                            .setOnlyAlertOnce(true)
                    }
                    notification?.let { nf ->
                        nm.notify(pushId, nf)
                    }
                    Timber.e(e)
                }
                pendingResult?.finish()
            }.invokeOnCompletion { scope.cancel() }
        }
    }

    private fun logPushAnswerTap(senderId: Long) {
        scope.launch {
            amplitudeHelper.logPushAnswerTap(userUidUseCase.invoke(), senderId)
        }

    }


    @SuppressLint("MissingPermission")
    private suspend fun handleResponse(data: Any?, pushId: Int, context: Context, message: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val myAvatar = getOwnLocalProfileUseCase.invoke()?.avatarSmall
            val me = context.getString(R.string.me)
            messageStyleNotificationCreator.addResponseMessage(
                ChatPushData(
                    roomId = pushId.toLong(),
                    message = message,
                    isGroupChat = false,
                    chatName = "",
                    user = UserChat().apply {
                        name = me
                        avatarSmall = myAvatar
                    }
                )
            )
            return
        }
        val nm = NotificationManagerCompat.from(context)
        val notification = getActiveNotification(pushId, context)
        var activeNotificationsCount = getActiveNotificationCount(context)
        notification?.let { nf -> nm.notify(pushId, nf) }
        if (data != null) {
            delay(DELAY_DELETE_NOTIFICATION.toLong())
            nm.cancel(pushId)
            activeNotificationsCount--
            if (activeNotificationsCount == 1) nm.cancelAll()
        } else {
            Timber.e("Error while sending reply in push")
        }
    }


    fun getActiveNotification(notificationId: Int, context: Context): Notification? {
        val notificationManager: NotificationManager? =
            context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager?
        val barNotifications = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            notificationManager?.activeNotifications
        } else {
            null
        }
        return barNotifications?.findLast { it.id == notificationId }?.notification
    }

    fun getActiveNotificationCount(context: Context): Int {
        val notificationManager: NotificationManager? =
            context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager?
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            notificationManager?.activeNotifications?.size ?: 0
        } else {
            0
        }
    }
}
