package com.numplates.nomera3.data.fcm

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import javax.inject.Inject

class NotificationConfig @Inject constructor() {
    private val firebaseConfig = FirebaseRemoteConfig.getInstance()
    //better to create config module and inject some interface into constructor
    fun getNotificationCommonType(): NotificationCommonType {
        return when (firebaseConfig.getLong(REMOTE_CONFIG_NOTIFICATION_TYPE)) {
            REMOTE_CONFIG_NOTIFICATION_TYPE_COMMON -> NotificationCommonType.COMMON_STYLE
            REMOTE_CONFIG_NOTIFICATION_TYPE_MESSENGER -> NotificationCommonType.MESSAGING_STYLE
            else -> NotificationCommonType.OLD
        }
    }

    fun isGroupingNotificationEnabled(): Boolean {
        return firebaseConfig.getBoolean(IS_GROUPING_COMMON_PUSH_ENABLED)
    }

    companion object {
        private const val REMOTE_CONFIG_NOTIFICATION_TYPE = "androidPushNitificationType"
        private const val REMOTE_CONFIG_NOTIFICATION_TYPE_COMMON = 1L
        private const val REMOTE_CONFIG_NOTIFICATION_TYPE_MESSENGER = 2L

        private const val IS_GROUPING_COMMON_PUSH_ENABLED = "pushGroupingEnabled"
    }
}

enum class NotificationCommonType {
    OLD, COMMON_STYLE, MESSAGING_STYLE
}
