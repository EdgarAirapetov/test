package com.numplates.nomera3.telecom

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.meera.db.models.dialog.UserChat
import com.numplates.nomera3.CALL_ACTION_ACCEPT_CALL
import com.numplates.nomera3.CALL_ACTION_OPEN_CALL
import com.numplates.nomera3.CALL_ACTION_REJECT_CALL
import com.numplates.nomera3.CALL_NOTIFICATION_ID
import com.numplates.nomera3.R
import com.numplates.nomera3.data.fcm.models.PushCallObject
import javax.inject.Inject

class CallNotificationManager @Inject constructor(
    private val appContext: Context,
    private val callIntentProvider: CallIntentProvider,
) {
    private val ringtoneAudioFocusManager = RingtoneAudioFocusManager(appContext)
    private val notificationManager: NotificationManager by lazy(mode = LazyThreadSafetyMode.NONE) {
        appContext.getSystemService(Service.NOTIFICATION_SERVICE) as NotificationManager
    }

    /**
     * Отменяем пуш о звонке по [CALL_NOTIFICATION_ID]
     * */
    fun cancelCallNotification() {
        ringtoneAudioFocusManager.releaseAudioFocus()
        notificationManager.cancel(CALL_NOTIFICATION_ID)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun createPushForIncomingCall(userChat: UserChat, callPushObject: PushCallObject) {
        ringtoneAudioFocusManager.requestAudioFocus()
        val notification = NotificationCompat.Builder(appContext, getCallNotificationChannelId())
            .setSmallIcon(R.drawable.splash_meera_logo)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setOpenCallFullscreenIntent(userChat = userChat, callPushObject = callPushObject)
            .setAutoCancel(true)
            .setContentTitle(appContext.getString(R.string.incoming_call))
            .setContentText(userChat.name)
            .addRejectCallAction(userChat = userChat, callPushObject = callPushObject)
            .addAcceptCallAction(userChat = userChat, callPushObject = callPushObject)
            .setSound(getCallNotificationAudioUri())
            .setVibrate(getCallNotificationVibrationPattern())
            .setOngoing(true)
            .build()
            .addLoopAudioFlag()
        notificationManager.notify(CALL_NOTIFICATION_ID, notification)
    }

    private fun Notification.addLoopAudioFlag() = this.apply {
        flags = flags or Notification.FLAG_INSISTENT
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getCallNotificationChannelId(): String {
        val channelName: String = appContext.getString(R.string.call_notifications)
        var callNotificationChannel: NotificationChannel? = notificationManager.getNotificationChannel(CALL_CHANNEL_ID)
        if (callNotificationChannel == null) {
            callNotificationChannel =
                NotificationChannel(CALL_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_HIGH)
            callNotificationChannel.setSound(
                getCallNotificationAudioUri(),
                ringtoneAudioFocusManager.getCallRingtoneAudioAttributes()
            )
            notificationManager.createNotificationChannel(callNotificationChannel)
        }
        return CALL_CHANNEL_ID
    }

    private fun NotificationCompat.Builder.setOpenCallFullscreenIntent(
        userChat: UserChat,
        callPushObject: PushCallObject
    ) = this.setFullScreenIntent(
        callIntentProvider.createFullScreenPendingIntent(
            callAction = CALL_ACTION_OPEN_CALL,
            userChat = userChat,
            pushObject = callPushObject,
        ),
        true,
    )

    private fun NotificationCompat.Builder.addRejectCallAction(userChat: UserChat, callPushObject: PushCallObject) =
        this.addAction(
            R.drawable.ic_sent_message,
            appContext.getString(R.string.call_reject),
            callIntentProvider.createRejectCallPendingIntent(
                callAction = CALL_ACTION_REJECT_CALL,
                userChat = userChat,
                pushObject = callPushObject,
            )
        )

    private fun NotificationCompat.Builder.addAcceptCallAction(userChat: UserChat, callPushObject: PushCallObject) =
        this.addAction(
            R.drawable.icon_arrow_gray,
            appContext.getString(R.string.call_accept),
            callIntentProvider.createAcceptCallPendingIntent(
                callAction = CALL_ACTION_ACCEPT_CALL,
                userChat = userChat,
                pushObject = callPushObject,
            )
        )

    private fun getCallNotificationVibrationPattern() = longArrayOf(
        0, 200, 100, 200, 1000, 0, 200, 100, 200, 1000, 0, 200, 100,
        200, 1000, 0, 200, 100, 200, 1000, 0, 200, 100, 200, 1000
    )

    private fun getCallNotificationAudioUri(): Uri =
        Uri.parse(
            ContentResolver.SCHEME_ANDROID_RESOURCE + "://" +
                appContext.packageName + "/raw/sound_incoming_call"
        )

    companion object {
        private const val CALL_CHANNEL_ID = "80008"
    }
}
