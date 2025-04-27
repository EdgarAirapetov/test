package com.numplates.nomera3.modules.notifications.ui.entity.model

sealed class NotificationClickActionButton {
    class OnAcceptFriendRequest(val notificationId: String) : NotificationClickActionButton()
    class OnTransitToChat(val roomId: Long?) : NotificationClickActionButton()
    class OnTransitToSendGift(val userId: Long, val userName: String) : NotificationClickActionButton()
}
