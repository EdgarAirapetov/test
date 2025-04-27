package com.numplates.nomera3.modules.baseCore.helper.notification

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import com.meera.core.preferences.AppSettings
import com.numplates.nomera3.BuildConfig
import com.numplates.nomera3.R
import kotlinx.coroutines.delay
import timber.log.Timber

class NotificationManagerImpl(
    private val appSettings: AppSettings,
    private val appContext: Context
) : NotificationManager {

    private val channelsHash by lazy {
        hashMapOf(
            CHANNEL_ID_MESSENGER to appContext.getString(R.string.push_notification_channel_messenger),
            CHANNEL_ID_FRIENDS to appContext.getString(R.string.push_notification_channel_friends),
            CHANNEL_ID_GIFTS to appContext.getString(R.string.push_notification_channel_gifts),
            CHANNEL_ID_COMMENTS to appContext.getString(R.string.push_notification_channel_comments),
            CHANNEL_ID_GROUP_REQUEST to appContext.getString(R.string.push_notification_channel_group_request),
            CHANNEL_ID_NOOMEERA to "Noomeera"
        )
    }

    private val channelsBase by lazy {
        hashMapOf(
            MESSENGER_BASE to appContext.getString(R.string.push_notification_channel_messenger),
            FRIENDS_BASE to appContext.getString(R.string.push_notification_channel_friends),
            GIFTS_BASE to appContext.getString(R.string.push_notification_channel_gifts),
            COMMENTS_BASE to appContext.getString(R.string.push_notification_channel_comments),
            GROUP_REQUEST_BASE to appContext.getString(R.string.push_notification_channel_group_request),
            NOOMEERA_BASE to "Noomeera"
        )
    }

    override fun getChannels(): HashMap<String, String> = channelsHash

    override suspend fun removeNotificationById(notificationId: Int) {
        val ns = Context.NOTIFICATION_SERVICE
        (appContext.getSystemService(ns) as? android.app.NotificationManager)?.cancel(notificationId)
        delay(DEFAULT_DELAY)
        if (hasOnlyGroup(appContext)) {
            (appContext.getSystemService(ns) as? android.app.NotificationManager)?.cancelAll()
        }
    }

    private fun hasOnlyGroup(context: Context): Boolean {
        val notificationManager: android.app.NotificationManager? =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager?
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val isFirstGroup =
                notificationManager?.activeNotifications?.firstOrNull()?.id == NOTIFICATION_GROUP_ID
            notificationManager?.activeNotifications?.size == 1 && isFirstGroup
        } else {
            false
        }
    }

    private val nm: android.app.NotificationManager =
        appContext.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager

    override fun deleteChannelsIfUpdatedApp() {
        try {
            val currentAppVer = BuildConfig.VERSION_CODE
            val lastRecordedAppVer = appSettings.lastRecordedAppCode
            if (currentAppVer == lastRecordedAppVer) return
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                deleteAllChannels(lastRecordedAppVer)
                appSettings.lastRecordedAppCode = currentAppVer
            }
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun deleteAllChannels(lastRecordedAppVer: Int) {
        channelsBase.forEach { entry ->
            val channelPostfix =
                if (lastRecordedAppVer == appSettings.lastRecordedAppCodeDefaultValue) ""
                else " $lastRecordedAppVer"
            nm.deleteNotificationChannel("${entry.key}$channelPostfix")
        }
        Timber.d("All channels deleted")
    }

    companion object {

        const val MESSENGER_BASE = "CHANNEL_ID_MESSENGER"
        const val FRIENDS_BASE = "CHANNEL_ID_FRIENDS"
        const val GIFTS_BASE = "CHANNEL_ID_GIFTS"
        const val COMMENTS_BASE = "CHANNEL_ID_COMMENTS"
        const val GROUP_REQUEST_BASE = "CHANNEL_ID_GROUP_REQUEST"
        const val NOOMEERA_BASE = "CHANNEL_ID_NOOMERA"
        const val GROUP_KEY_ALL_BASE = "com.numplates.nomera3.ALL"

        const val DEFAULT_DELAY = 1000L
        const val NOTIFICATION_GROUP_ID = -10

        private const val CHANNEL_ID_MESSENGER = "$MESSENGER_BASE ${BuildConfig.VERSION_CODE}"
        private const val CHANNEL_ID_FRIENDS = "$FRIENDS_BASE ${BuildConfig.VERSION_CODE}"
        private const val CHANNEL_ID_GIFTS = "$GIFTS_BASE ${BuildConfig.VERSION_CODE}"
        private const val CHANNEL_ID_COMMENTS = "$COMMENTS_BASE ${BuildConfig.VERSION_CODE}"
        private const val CHANNEL_ID_GROUP_REQUEST =
            "$GROUP_REQUEST_BASE ${BuildConfig.VERSION_CODE}"
        private const val CHANNEL_ID_NOOMEERA = "$NOOMEERA_BASE ${BuildConfig.VERSION_CODE}"
    }
}

fun getChannelIdForBase(channel: String): String {
    return when (channel) {
        NotificationManagerImpl.MESSENGER_BASE -> "${NotificationManagerImpl.MESSENGER_BASE} ${BuildConfig.VERSION_CODE}"
        NotificationManagerImpl.FRIENDS_BASE -> "${NotificationManagerImpl.FRIENDS_BASE} ${BuildConfig.VERSION_CODE}"
        NotificationManagerImpl.GIFTS_BASE -> "${NotificationManagerImpl.GIFTS_BASE} ${BuildConfig.VERSION_CODE}"
        NotificationManagerImpl.COMMENTS_BASE -> "${NotificationManagerImpl.COMMENTS_BASE} ${BuildConfig.VERSION_CODE}"
        NotificationManagerImpl.GROUP_REQUEST_BASE -> "${NotificationManagerImpl.GROUP_REQUEST_BASE} ${BuildConfig.VERSION_CODE}"
        NotificationManagerImpl.NOOMEERA_BASE -> "${NotificationManagerImpl.NOOMEERA_BASE} ${BuildConfig.VERSION_CODE}"
        NotificationManagerImpl.GROUP_KEY_ALL_BASE -> "${NotificationManagerImpl.GROUP_KEY_ALL_BASE} ${BuildConfig.VERSION_CODE}"
        else -> ""
    }
}
