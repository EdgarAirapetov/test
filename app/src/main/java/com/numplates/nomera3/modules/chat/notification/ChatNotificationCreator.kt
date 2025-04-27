package com.numplates.nomera3.modules.chat.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.drawable.Icon
import android.media.AudioAttributes
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.Person
import androidx.core.app.RemoteInput
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.IconCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.meera.core.di.scopes.AppScope
import com.meera.core.extensions.dp
import com.meera.core.utils.files.FileManager
import com.numplates.nomera3.R
import com.numplates.nomera3.data.fcm.CommonNotificationCreatorImpl
import com.numplates.nomera3.data.fcm.NotificationHelper
import com.numplates.nomera3.data.fcm.NotificationReceiver
import com.numplates.nomera3.modules.baseCore.helper.notification.NotificationManagerImpl
import com.numplates.nomera3.modules.baseCore.helper.notification.NotificationManagerImpl.Companion.MESSENGER_BASE
import com.numplates.nomera3.modules.baseCore.helper.notification.NotificationManagerImpl.Companion.NOTIFICATION_GROUP_ID
import com.numplates.nomera3.modules.baseCore.helper.notification.getChannelIdForBase
import com.numplates.nomera3.modules.redesign.MeeraAct
import com.numplates.nomera3.presentation.router.IActionContainer
import com.numplates.nomera3.presentation.router.IArgContainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import kotlin.random.Random
import androidx.core.net.toUri

@AppScope
class ChatNotificationCenter @Inject constructor(
    private val context: Context,
    private val fileUtils: FileManager
) : MessageStyleNotificationCreator {

    private val notificationMessageSoundUri =
        (ContentResolver.SCHEME_ANDROID_RESOURCE + "://" +
            context.applicationContext.packageName + "/raw/message").toUri()

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    private val channelId = getChannelIdForBase(MESSENGER_BASE)

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    @RequiresApi(Build.VERSION_CODES.P)
    override fun showNotification(data: ChatPushData) {
        createNotificationChannel()

        if (data.isResended != true) {
            val recoveredNotification = recoverNotificationForId(data.roomId.toInt())

            recoveredNotification?.let { notification ->
                scope.getAvatarBitmapForNotification(context, data.user?.avatarSmall) { avatar ->
                    appendMessageForNotification(
                        notification,
                        data.message,
                        data.roomId.toInt(),
                        android.app.Person.Builder()
                            .setKey(data.user?.userId?.toString())
                            .setName(data.user?.name ?: "unknown")
                            .setIcon(Icon.createWithBitmap(avatar))
                            .build(),
                        true,
                        data.image
                    )
                }
            } ?: kotlin.run {
                createNotificationAndShow(data)
            }
        } else {
            createNotificationAndShow(data, true)
        }
    }

    // used to add my message from Notification receiver
    @RequiresApi(Build.VERSION_CODES.P)
    override fun addResponseMessage(data: ChatPushData) {
        createNotificationChannel()
        recoverNotificationForId(data.roomId.toInt())?.let { recoveredNotification ->
            scope.getAvatarBitmapForNotification(context, data.user?.avatarSmall) { avatar ->
                appendMessageForNotification(
                    notification = recoveredNotification,
                    message = data.message,
                    person = android.app.Person.Builder()
                        .setKey(data.user?.userId?.toString())
                        .setName(data.user?.name ?: "")
                        .setIcon(Icon.createWithBitmap(avatar))
                        .build(),
                    notificationId = data.roomId.toInt(),
                    shouldNotify = false
                )
            }
        }
    }

    private fun getMessageForImage(
        data: ChatPushData,
        person: Person
    ): NotificationCompat.MessagingStyle.Message? {
        return try {
            if (data.image.isNullOrEmpty()) {
                null
            } else {
                val image = getBigImageFuture(data.image).get()
                val uri = fileUtils.getUriFromBitmap(context, image)
                NotificationCompat
                    .MessagingStyle
                    .Message("", System.currentTimeMillis(), person)
                    .setData("image/", uri)
            }
        } catch (e: Exception) {
            Timber.e(e)
            null
        }
    }

    private fun createNotificationAndShow(data: ChatPushData, isRandomId: Boolean = false) {
        scope.launch(Dispatchers.IO) {
            val person = Person.Builder()
                .setKey(data.user?.userId?.toString())
                .setName(data.user?.name ?: "")
                .apply {
                    runCatching {
                        val avatar = getAvatarFuture(data.user?.avatarSmall).get()
                        if (data.user?.avatarSmall != null) setIcon(IconCompat.createWithBitmap(avatar))
                    }
                }
                .build()
            val message = getMessageForImage(data, person)
            val style = NotificationCompat.MessagingStyle(person)
                .setConversationTitle(data.chatName)
                .addMessage(data.message, System.currentTimeMillis(), person)
            message?.let { style.addMessage(it) }
            val notification = NotificationCompat
                .Builder(context, channelId)
                .setStyle(style)
                .setSmallIcon(R.drawable.splash_meera_logo)
                .setAutoCancel(true)
                .setContentIntent(getPendingIntent(data))
                .setOnlyAlertOnce(true)
                .apply {
                    color = ContextCompat.getColor(context, R.color.ui_light_green)
                    data.user?.userId?.let {
                        if (data.showReply == true) {
                            addAction(getActionReply(it, data.roomId, data.roomId.toInt()))
                        }
                    }
                }
                .build()
            notificationManager.notify(
                if (isRandomId) {
                    Random.nextInt()
                } else {
                    data.roomId.toInt()
                }, notification
            )
        }
    }

    private fun getBundleForNotification(data: ChatPushData) = Bundle().apply {
        putLong(IArgContainer.ARG_ROOM_ID, data.roomId)
        putString(IArgContainer.ARG_PUSH_EVENT_ID, data.eventId)
    }

    private fun getPendingIntent(data: ChatPushData): PendingIntent {
        val intentFilter = IntentFilter()
        intentFilter.addAction(IActionContainer.ACTION_OPEN_CHAT)
        val intent = Intent(context, MeeraAct::class.java).apply {
            action = IActionContainer.ACTION_OPEN_CHAT
            putExtras(getBundleForNotification(data))
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        return PendingIntent.getActivity(
            context,
            data.roomId.toInt(),
            intent,
            getPendingIntentFlag()
        )
    }

    private fun getActionReply(
        senderId: Long,
        roomId: Long,
        pushID: Int,
    ): NotificationCompat.Action {
        val replyLabel = context.getString(R.string.road_reply_comment)
        val remoteInput: RemoteInput = RemoteInput.Builder(NotificationHelper.KEY_TEXT_REPLY).run {
            setLabel(replyLabel)
            build()
        }
        val replyIntent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra(NotificationHelper.KEY_SENDER_ID, senderId)
            putExtra(NotificationHelper.KEY_ROOM_ID, roomId)
            putExtra(NotificationHelper.KEY_PUSH_ID, pushID)
        }
        val replyPendingIntent: PendingIntent = PendingIntent.getBroadcast(
            context,
            pushID,
            replyIntent,
            getPendingIntentFlag()
        )

        return NotificationCompat.Action
            .Builder(R.drawable.send_message, replyLabel, replyPendingIntent)
            .addRemoteInput(remoteInput)
            .setAllowGeneratedReplies(true)
            .build()
    }

    private fun getPendingIntentFlag(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun appendMessageForNotification(
        notification: Notification,
        message: String,
        notificationId: Int,
        person: android.app.Person,
        shouldNotify: Boolean = true,
        image: String? = null
    ) {
        scope.launch(Dispatchers.IO) {
            val recoveredNotificationBuilder =
                Notification.Builder
                    .recoverBuilder(context, notification)
                    .setOnlyAlertOnce(!shouldNotify)
            recoveredNotificationBuilder.also { builder ->
                val messageStyle = builder.style as? Notification.MessagingStyle
                messageStyle?.addMessage(
                    message,
                    System.currentTimeMillis(),
                    person
                )
                if (!image.isNullOrEmpty()) {
                    runCatching {
                        val imageBtm = getBigImageFuture(image).get()
                        val uri = fileUtils.getUriFromBitmap(context, imageBtm)
                        val messageImg = Notification
                            .MessagingStyle
                            .Message("", System.currentTimeMillis(), person)
                            .setData("image/", uri)
                        messageStyle?.addMessage(messageImg)
                    }
                }
                messageStyle?.let { builder.style = messageStyle }
            }
            notificationManager.notify(notificationId, recoveredNotificationBuilder.build())
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun recoverNotificationForId(notificationId: Int): Notification? {
        val statusBarNotification = notificationManager
            .activeNotifications
            .firstOrNull {
                it.id == notificationId
            } ?: return null
        return statusBarNotification.notification
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
            val name = context.getString(R.string.push_notification_channel_messenger)
            val channel =
                NotificationChannel(channelId, name, NotificationManager.IMPORTANCE_HIGH).apply {
                    setSound(notificationMessageSoundUri, audioAttributes)
                    enableVibration(true)
                    lightColor = ContextCompat.getColor(context, R.color.ui_light_green)
                    enableLights(true)
                }
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun getAvatarFuture(avatar: String?) =
        Glide.with(context).asBitmap().load(avatar)
            .override(PUSH_AVATAR_SIZE.dp).apply(
                RequestOptions.circleCropTransform().placeholder(R.drawable.fill_8_round)
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
            ).submit()

    private fun getBigImageFuture(avatar: String) =
        Glide.with(context).asBitmap().load(avatar)
            .override(CommonNotificationCreatorImpl.BIG_IMAGE_MAX_SIZE.dp)
            .submit()

    override fun createGroup(chanelId: String) {
        val builder = NotificationCompat.Builder(context, chanelId)
        builder.apply {
            setSmallIcon(R.drawable.splash_meera_logo)
            color = ContextCompat.getColor(context, R.color.ui_light_green)
            setContentInfo("Noomeera")
            setGroupSummary(true)
            setGroup(getChannelIdForBase(NotificationManagerImpl.GROUP_KEY_ALL_BASE))
        }
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_GROUP_ID, builder.build())
    }
}
