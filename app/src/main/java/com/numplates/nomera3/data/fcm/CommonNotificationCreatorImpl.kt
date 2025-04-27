package com.numplates.nomera3.data.fcm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioAttributes
import androidx.core.app.NotificationCompat
import androidx.core.app.Person
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.IconCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.meera.core.extensions.dp
import com.meera.core.preferences.AppSettings
import com.meera.core.utils.files.FileManager
import com.meera.core.utils.getNotificationPendingIntentFlag
import com.numplates.nomera3.R
import com.numplates.nomera3.data.fcm.data.CommonPushModel
import com.numplates.nomera3.modules.baseCore.helper.notification.NotificationManagerImpl
import com.numplates.nomera3.modules.baseCore.helper.notification.getChannelIdForBase
import com.numplates.nomera3.modules.chat.notification.PUSH_AVATAR_SIZE
import com.numplates.nomera3.modules.redesign.MeeraAct
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject
import androidx.core.net.toUri

class CommonNotificationCreatorImpl @Inject constructor(
    private val context: Context,
    private val appSetting: AppSettings,
    private val nm: com.numplates.nomera3.modules.baseCore.helper.notification.NotificationManager,
    private val config: NotificationConfig,
    private val fileUtils: FileManager
) : CommonNotificationCreator {

    private val notificationMessageSoundUri =
        (CONTENT_RESOLVER_SCHEME + context.applicationContext.packageName + RAW_PUSH_PATH).toUri()

    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun showNotification(data: CommonPushModel) {
        scope.launch {
            val id = getNotificationIdAndInc()
            val channelId = getChannelIdForBase(data.channelIdBase)
            createChannel(channelId)
            if (config.isGroupingNotificationEnabled()) {
                createGroup(channelId)
            }

            val notification = NotificationCompat.Builder(context, channelId)

            val selfPerson = Person.Builder().setKey(OTHER_KEY).setName(OTHER_NAME).build()

            val senderPersonBuilder = Person.Builder().setKey(data.userId?.toString()).setName(data.contentTitle)

            if (!data.avatar.isNullOrEmpty()) {
                try {
                    val avatarFuture = getAvatarFuture(data.avatar)
                    senderPersonBuilder.setIcon(IconCompat.createWithBitmap(avatarFuture.get()))
                } catch (e: Exception) {
                    Timber.e(e)
                }
            }

            val style = NotificationCompat.MessagingStyle(selfPerson).setConversationTitle(EMPTY_STRING)
                .addMessage(data.contentText, System.currentTimeMillis(), senderPersonBuilder.build())

            if (!data.bigImage.isNullOrEmpty()) {
                try {
                    val image = getBigImageFuture(data.bigImage).get()
                    val uri = fileUtils.getUriFromBitmap(context, image)
                    val imageMessage = NotificationCompat.MessagingStyle.Message(
                        EMPTY_STRING, System.currentTimeMillis(), senderPersonBuilder.build()
                    ).setData(IMAGE_MIME_TYPE, uri)
                    style.addMessage(imageMessage)
                } catch (e: Exception) {
                    Timber.e(e)
                }
            }

            style.isGroupConversation = true

            notification.setStyle(style)
            notification.applyDefault(data)

            notificationManager.notify(id, notification.build())
        }
    }

    fun getNotificationIdAndInc(): Int {
        val id = appSetting.readPushId() + 1
        appSetting.writePushIdNew(id)
        return id
    }

    private fun NotificationCompat.Builder.applyDefault(
        data: CommonPushModel
    ) = this.setContentTitle(data.contentTitle).setContentText(data.contentText)
        .setSmallIcon(R.drawable.splash_meera_logo).setWhen(System.currentTimeMillis())
        .setColor(ContextCompat.getColor(context, R.color.ui_light_green)).setVibrate(VIBRATION_PATTERN)
        .setContentIntent(getPendingIntent(data)).setSound(notificationMessageSoundUri).apply {
            if (config.isGroupingNotificationEnabled()) {
                setGroup(getChannelIdForBase(NotificationManagerImpl.GROUP_KEY_ALL_BASE))
            } else {
                setGroup(UUID.randomUUID().toString())
            }
        }

    private fun getPendingIntent(
        model: CommonPushModel,
    ): PendingIntent {
        val intentFilter = IntentFilter()
        intentFilter.addAction(model.action)
        val intent = Intent(context, MeeraAct::class.java)
        intent.action = model.action
        intent.putExtras(model.prepareBundle())
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        return PendingIntent.getActivity(
            context, model.id, intent, getNotificationPendingIntentFlag()
        )
    }

    private fun getAvatarFuture(avatar: String?) =
        Glide.with(context).asBitmap().load(avatar).override(PUSH_AVATAR_SIZE.dp).apply(
            RequestOptions.circleCropTransform().placeholder(R.drawable.fill_8_round)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
        ).submit()

    private fun getBigImageFuture(avatar: String) =
        Glide.with(context).asBitmap().load(avatar).override(BIG_IMAGE_MAX_SIZE.dp).submit()

    private fun createChannel(channelId: String) {
        val audioAttributes = AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_NOTIFICATION).build()
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(channelId, nm.getChannels()[channelId], importance)
        channel.setSound(notificationMessageSoundUri, audioAttributes)
        channel.enableVibration(true)
        channel.lightColor = ContextCompat.getColor(context, R.color.ui_light_green)
        channel.enableLights(true)
        notificationManager.createNotificationChannel(channel)
    }

    private fun createGroup(chanelId: String) {
        val builder = NotificationCompat.Builder(context, chanelId)

        builder.apply {
            color = ContextCompat.getColor(context, R.color.ui_light_green)
            setSmallIcon(R.drawable.splash_meera_logo)
            setContentInfo(GROUP_CONTENT_INFO)
            setGroupSummary(true)
            setGroup(getChannelIdForBase(NotificationManagerImpl.GROUP_KEY_ALL_BASE))
        }

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NotificationManagerImpl.NOTIFICATION_GROUP_ID, builder.build())
    }

    companion object {
        const val BIG_IMAGE_MAX_SIZE = 100
        const val GROUP_CONTENT_INFO = "Noomeera"
        const val PERSON_KEY = "person_common_key"
        const val OTHER_KEY = "other_user_key"
        const val OTHER_NAME = "other_user_name"

        private const val RAW_PUSH_PATH = "/raw/push"
        private const val CONTENT_RESOLVER_SCHEME = ContentResolver.SCHEME_ANDROID_RESOURCE + "://"

        private const val EMPTY_STRING = ""
        private const val IMAGE_MIME_TYPE = "image/*"

        private val VIBRATION_PATTERN = longArrayOf(1000, 1000)
    }
}
