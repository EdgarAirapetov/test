package com.numplates.nomera3.data.fcm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentResolver
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.core.app.NotificationCompat
import androidx.core.app.RemoteInput
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.meera.core.extensions.doAsync
import com.meera.core.extensions.dpToPx
import com.meera.core.extensions.toBoolean
import com.meera.core.preferences.AppSettings
import com.meera.core.utils.getNotificationPendingIntentFlag
import com.meera.db.models.dialog.UserChat
import com.numplates.nomera3.Act
import com.numplates.nomera3.App
import com.numplates.nomera3.R
import com.numplates.nomera3.data.fcm.IPushInfo.CHAT_INCOMING_MESSAGE
import com.numplates.nomera3.modules.baseCore.helper.notification.NotificationManagerImpl.Companion.GROUP_KEY_ALL_BASE
import com.numplates.nomera3.modules.baseCore.helper.notification.NotificationManagerImpl.Companion.MESSENGER_BASE
import com.numplates.nomera3.modules.baseCore.helper.notification.getChannelIdForBase
import com.numplates.nomera3.modules.chat.notification.ChatPushData
import com.numplates.nomera3.modules.chat.notification.MessageStyleNotificationCreator
import com.numplates.nomera3.modules.reaction.data.ReactionType
import com.numplates.nomera3.presentation.router.IActionContainer
import com.numplates.nomera3.presentation.router.IArgContainer
import timber.log.Timber
import javax.inject.Inject
import androidx.core.net.toUri

private const val SHOW_REPLY_DEFAULT_VALUE = true

class NotificationHelper @Inject constructor(private val context: Context) {

    @Inject
    lateinit var appSettings: AppSettings

    @Inject
    lateinit var messageStyleNotification: MessageStyleNotificationCreator

    @Inject
    lateinit var notificationCreator: CommonNotificationCreator

    @Inject
    lateinit var nm: com.numplates.nomera3.modules.baseCore.helper.notification.NotificationManager

    @Inject
    lateinit var notificationConfig: NotificationConfig

    init {
        App.component.inject(this)
    }

    fun show(action: String, channelIdBase: String, data: PushObjectNew) {
        val pushObject = data.copy(title = data.title ?: context.getString(R.string.general_noomeera))
        val channelId = getChannelIdForBase(channelIdBase)
        val canShowMessagingNotification =
            pushObject.roomId != null && channelIdBase == MESSENGER_BASE && pushObject.type == CHAT_INCOMING_MESSAGE
        if (canShowMessagingNotification && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            messageStyleNotification.showNotification(
                ChatPushData(
                    roomId = pushObject.roomId ?: 0L,
                    message = pushObject.message ?: "",
                    chatName = pushObject.chatName ?: "",
                    isGroupChat = pushObject.isGroupChat ?: false,
                    user = UserChat(
                        userId = pushObject.senderId,
                        name = pushObject.userName,
                        avatarSmall = pushObject.senderAvatar
                    ),
                    eventId = pushObject.eventId,
                    image = pushObject.attachmentUrl,
                    showReply = pushObject.showReply?.toBoolean() ?: SHOW_REPLY_DEFAULT_VALUE,
                    isResended = pushObject.isResended ?: false
                )
            )
            return
        }

        val notificationManager =
            context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        val isNewNotificationsEnabled = notificationConfig.getNotificationCommonType() != NotificationCommonType.OLD
        if (!isNewNotificationsEnabled) {
            createChannel(notificationManager, channelId)
        } else {
            Timber.d("showNotification: new notification")
            notificationCreator.showNotification(pushObject.toCommonPushModel(action, channelIdBase))
            return
        }

        var pushID = appSettings.readPushId()
        pushID++

        val builder = NotificationCompat.Builder(context, channelId)
        builder.apply {
            Timber.d("setContentTitle = ${pushObject.title}")
            setContentTitle(pushObject.title)
            Timber.d("setContentText = ${pushObject.description}")
            setContentText(pushObject.description)
            setSmallIcon(R.drawable.splash_meera_logo)
            color = ContextCompat.getColor(context, R.color.ui_light_green)
            setWhen(System.currentTimeMillis())
            setVibrate(longArrayOf(1000, 1000))
            setGroup(getChannelIdForBase(GROUP_KEY_ALL_BASE))
            setContentIntent(getPendingIntent(context, action, pushObject, pushID))
            setAutoCancel(true)
            if (channelIdBase == MESSENGER_BASE) {
                addActionReply(pushObject, pushID, this)
            }
            setImage(true, pushObject) {
                createGroup(channelId)
                notificationManager.notify(pushID, builder.build())
            }
        }

        appSettings.writePushIdNew(pushID)
    }

    private fun addActionReply(
        pushObject: PushObjectNew,
        pushID: Int,
        builder: NotificationCompat.Builder
    ) {
        val replyLabel = context.getString(R.string.road_reply_comment)
        val remoteInput: RemoteInput = RemoteInput.Builder(KEY_TEXT_REPLY).run {
            setLabel(replyLabel)
            build()
        }
        val replyIntent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra(KEY_SENDER_ID, pushObject.senderId)
            putExtra(KEY_ROOM_ID, pushObject.roomId)
            putExtra(KEY_PUSH_ID, pushID)
        }
        val replyPendingIntent: PendingIntent = PendingIntent.getBroadcast(
            context,
            pushID,
            replyIntent,
            getNotificationPendingIntentFlag()
        )

        val replyAction: NotificationCompat.Action =
            NotificationCompat.Action.Builder(
                R.drawable.send_button,
                replyLabel, replyPendingIntent
            )
                .addRemoteInput(remoteInput)
                .setAllowGeneratedReplies(true)
                .build()
        builder.setOnlyAlertOnce(true)
        builder.addAction(replyAction)

    }

    private fun NotificationCompat.Builder.setImage(
        showExpandedImage: Boolean,
        pushObject: PushObjectNew,
        builder: (builder: NotificationCompat.Builder) -> Unit
    ) {
        val postImageUrl: String? = pushObject.imageUrl
        val avatarSmall: String? = pushObject.user?.avatarSmall

        when {
            postImageUrl != null -> {
                val futureTarget = Glide.with(context)
                    .asBitmap()
                    .load(postImageUrl)
                    .submit()

                doAsync({
                    return@doAsync try {
                        futureTarget.get()
                    } catch (e: Exception) {
                        Timber.e(e)
                        null
                    }
                }, { bitmapNull ->
                    bitmapNull?.let { bitmap ->
                        Timber.e("BITMAP bytes: ${bitmap.byteCount}")
                        if (showExpandedImage) {
                            this.setStyle(
                                NotificationCompat.BigPictureStyle()
                                    .bigPicture(bitmap)
                            )
                        }
                        this.setLargeIcon(bitmap)
                    }
                    Glide.with(context).clear(futureTarget)
                    builder(this)
                })
            }

            avatarSmall != null -> {
                val futureTarget = Glide.with(context)
                    .asBitmap()
                    .load(avatarSmall)
                    .override(dpToPx(64))
                    .apply(
                        RequestOptions
                            .circleCropTransform()
                            .placeholder(R.drawable.fill_8_round)
                            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    )
                    .submit()

                doAsync({
                    return@doAsync try {
                        futureTarget.get()
                    } catch (e: Exception) {
                        Timber.e(e)
                        null
                    }
                }, { bitmapNull ->
                    bitmapNull?.let { bitmap ->
                        Timber.e("BITMAP bytes: ${bitmap.byteCount}")
                        this.setStyle(
                            NotificationCompat.BigTextStyle()
                                .bigText(pushObject.description)
                        )
                        this.setLargeIcon(bitmap)
                    }
                    Glide.with(context).clear(futureTarget)
                    builder(this)
                })
            }

            else -> {
                builder(this)
            }
        }
    }

    private fun createGroup(chanelId: String) =
        messageStyleNotification.createGroup(chanelId)


    private fun createChannel(
        notificationManager: NotificationManager,
        channelId: String
    ) {
        // Creating an Audio Attribute
        Timber.d("channelId = $channelId")
        val uri = getUriSoundForChannel(channelId)

        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
            .build()

        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(channelId, nm.getChannels()[channelId], importance)
        channel.setSound(uri, audioAttributes)
        channel.enableVibration(true)
        channel.lightColor = ContextCompat.getColor(context, R.color.ui_light_green)
        channel.enableLights(true)
        notificationManager.createNotificationChannel(channel)
    }


    private fun getUriSoundForChannel(channelId: String): Uri {
        val resource = if (channelId == getChannelIdForBase(MESSENGER_BASE)) RAW_MESSAGE_PATH else RAW_PUSH_PATH
        return (CONTENT_RESOLVER_SCHEME + context.applicationContext.packageName + resource).toUri()
    }

    // Notification TAP action
    private fun getPendingIntent(
        context: Context,
        action: String,
        pushObject: PushObjectNew,
        uniqueId: Int
    ): PendingIntent {
        val intentFilter = IntentFilter()
        intentFilter.addAction(action)
        val intent = Intent(context, Act::class.java)
        intent.action = action
        intent.putExtras(prepareBundle(action, pushObject))
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        return PendingIntent.getActivity(
            context,
            uniqueId,
            intent,
            getNotificationPendingIntentFlag()
        )
    }


    private fun prepareBundle(action: String, pushObject: PushObjectNew): Bundle {
        Timber.e("PUSH_OBJECT: $pushObject")
        val bundle = Bundle()
        when (action) {
            IActionContainer.ACTION_OPEN_MOMENT -> {
                Timber.d("pushObject ACTION_OPEN_MOMENT = ${pushObject.momentId ?: 0L}")
                bundle.putLong(IArgContainer.ARG_MOMENT_ID, pushObject.momentId ?: 0L)
                bundle.putLong(IArgContainer.ARG_MOMENT_AUTHOR_ID, pushObject.momentAuthorId ?: 0L)
                if (pushObject.commentId != null) {
                    bundle.putLong(IArgContainer.ARG_COMMENT_ID, pushObject.commentId ?: 0L)
                }
            }

            IActionContainer.ACTION_OPEN_CHAT -> {
                Timber.d("pushObjectId ACTION_OPEN_CHAT = ${pushObject.roomId ?: 0L}")
                bundle.putLong(IArgContainer.ARG_ROOM_ID, pushObject.roomId ?: 0L)
                bundle.putString(IArgContainer.ARG_PUSH_EVENT_ID, pushObject.eventId)
            }

            IActionContainer.ACTION_FRIEND_REQUEST -> {
                val user = UserChat(pushObject.userId ?: 0)
                bundle.putParcelable(IArgContainer.ARG_USER_MODEL, user)
            }

            IActionContainer.ACTION_CALL_UNAVAILABLE -> {
                val user = UserChat(pushObject.userId ?: 0)
                bundle.putParcelable(IArgContainer.ARG_USER_MODEL, user)
            }

            IActionContainer.ACTION_FRIEND_CONFIRM -> {
                val user = UserChat(pushObject.userId ?: 0)
                bundle.putParcelable(IArgContainer.ARG_USER_MODEL, user)
            }

            IActionContainer.ACTION_OPEN_GIFTS -> {
                val user = UserChat(pushObject.userId ?: 0)
                bundle.putParcelable(IArgContainer.ARG_USER_MODEL, user)
            }

            IActionContainer.ACTION_LEAVE_POST_COMMENTS -> {
                bundle.putLong(IArgContainer.ARG_FEED_POST_ID, pushObject.postId ?: 0L)
                bundle.putLong(IArgContainer.ARG_COMMENT_ID, pushObject.commentId ?: 0L)
            }

            IActionContainer.ACTION_LEAVE_POST_COMMENT_REACTIONS -> {
                bundle.putLong(IArgContainer.ARG_FEED_POST_ID, pushObject.postId ?: 0L)
                bundle.putLong(IArgContainer.ARG_COMMENT_ID, pushObject.commentId ?: 0L)
                bundle.putSerializable(
                    IArgContainer.ARG_COMMENT_LAST_REACTION,
                    ReactionType.getByString(pushObject.lastReaction)
                )
            }

            IActionContainer.ACTION_REPLY_POST_COMMENTS ->
                bundle.putLong(IArgContainer.ARG_FEED_POST_ID, pushObject.postId ?: 0L)

            IActionContainer.ACTION_ADD_TO_GROUP_CHAT -> {
                Timber.d("pushObjectId ACTION_ADD_TO_GROUP_CHAT = ${pushObject.roomId ?: 0L}")
                bundle.putLong(IArgContainer.ARG_ROOM_ID, pushObject.roomId ?: 0L)
            }

            IActionContainer.ACTION_REQUEST_TO_GROUP -> {
                bundle.putInt(IArgContainer.ARG_GROUP_ID, pushObject.groupId ?: -1)
            }

            IActionContainer.ACTION_OPEN_POST -> {
                bundle.putLong(IArgContainer.ARG_FEED_POST_ID, pushObject.postId ?: 0L)
            }

            IActionContainer.ACTION_OPEN_POST_WITH_REACTIONS -> {
                bundle.putLong(IArgContainer.ARG_FEED_POST_ID, pushObject.postId ?: 0L)
                bundle.putBoolean(IArgContainer.ARG_FEED_POST_HAVE_REACTIONS, true)
                bundle.putSerializable(
                    IArgContainer.ARG_POST_LATEST_REACTION_TYPE,
                    ReactionType.getByString(pushObject.lastReaction)
                )
            }

            IActionContainer.ACTION_OPEN_GALLERY_WITH_REACTIONS -> {
                bundle.putLong(IArgContainer.ARG_FEED_POST_ID, pushObject.postId ?: 0L)
                bundle.putBoolean(IArgContainer.ARG_IS_PROFILE_PHOTO, false)
                bundle.putBoolean(IArgContainer.ARG_IS_OWN_PROFILE, true)
            }

            IActionContainer.ACTION_SYSTEM_EVENT -> {
                bundle.putString(IArgContainer.ARG_URL, pushObject.url ?: "")
            }

            IActionContainer.ACTION_OPEN_BIRTHDAY_GIFTS -> {
                val user = UserChat(userId = pushObject.userId ?: 0, name = pushObject.name)
                bundle.putParcelable(IArgContainer.ARG_USER_MODEL, user)
            }

            IActionContainer.ACTION_OPEN_BIRTHDAY_GROUP -> {
                bundle.putString(IArgContainer.ARG_EVENT_GROUP_ID, pushObject.eventGroupId)
            }

            IActionContainer.ACTION_OPEN_EVENT_ON_MAP -> {
                bundle.putLong(IArgContainer.ARG_FEED_POST_ID, pushObject.postId ?: -1L)
            }

            IActionContainer.ACTION_OPEN_PEOPLE -> {
                bundle.putLong(IArgContainer.ARG_USER_ID, pushObject.userId ?: -1L)
            }
        }
        return bundle
    }

    companion object {
        const val KEY_TEXT_REPLY = "key_text_reply"
        const val KEY_PUSH_ID = "key_push_id"
        const val KEY_ROOM_ID = "key_room_id"
        const val KEY_SENDER_ID = "key_sender_id"

        // Notification sound paths
        private const val RAW_MESSAGE_PATH = "/raw/message"
        private const val RAW_PUSH_PATH = "/raw/push"
        private const val CONTENT_RESOLVER_SCHEME = ContentResolver.SCHEME_ANDROID_RESOURCE + "://"
    }
}
