package com.numplates.nomera3.modules.baseCore.helper.notification

interface NotificationManager {
    fun deleteChannelsIfUpdatedApp()
    fun getChannels(): HashMap<String, String>
    suspend fun removeNotificationById(notificationId: Int)
}
