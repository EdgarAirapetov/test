package com.numplates.nomera3.modules.chat.helpers.resendmessage

import android.content.Context
import com.meera.core.extensions.empty
import com.meera.db.DataStore
import com.numplates.nomera3.R
import com.numplates.nomera3.data.fcm.NotificationHelper
import com.numplates.nomera3.data.fcm.PushObjectNew
import com.numplates.nomera3.data.newmessenger.ROOM_TYPE_DIALOG
import com.numplates.nomera3.data.newmessenger.ROOM_TYPE_GROUP
import com.numplates.nomera3.modules.baseCore.helper.notification.NotificationManagerImpl.Companion.MESSENGER_BASE
import com.numplates.nomera3.presentation.router.IActionContainer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject


class ResendNotificationUtil @Inject constructor(
    private val appContext: Context,
    private val dataStore: DataStore,
    private val notificationHelper: NotificationHelper
) {

    /**
     * Отправляем только 1 раз Пуш о том, что имеются
     * неотправленные сообщения.
     * Пуш отправляется на КАЖДОЕ неотправленное сообщение
     */
    suspend fun showNoSentMessagesNotifications() = withContext(Dispatchers.IO) {
        val unsentMessages = dataStore.messageDao().getAllUnsentMessages()
        unsentMessages.forEach { message ->
            message?.let { msg ->
                if (!msg.isResendShowPush) {
                    showNoSentMessagesNotification(msg.roomId)
                    msg.isResendShowPush = true
                    dataStore.messageDao().insert(msg)
                }
            }
        }
    }

    /**
     * Уведомление пользователя о том, что у него имеются
     * неотправленные сообщения
     */
    private fun showNoSentMessagesNotification(roomId: Long) {
        val chatName = getChatName(roomId)
        val appName = appContext.getString(R.string.application_name)
        val contentText = appContext.getString(
            R.string.notification_no_sent_messages_exists,
            chatName
        )

        val pushObject = PushObjectNew.unsentMessages(
            roomId = roomId,
            title = appName,
            description = contentText
        )
        notificationHelper.show(
            IActionContainer.ACTION_OPEN_CHAT,
            MESSENGER_BASE,
            pushObject
        )
    }

    private fun getChatName(roomId: Long): String {
        val currentRoom = dataStore.dialogDao().getDialogByRoomIdSuspend(roomId)
        return when(currentRoom?.type){
            ROOM_TYPE_DIALOG -> currentRoom.companion.name ?: String.empty()
            ROOM_TYPE_GROUP -> currentRoom.title ?: String.empty()
            else -> String.empty()
        }
    }

}
