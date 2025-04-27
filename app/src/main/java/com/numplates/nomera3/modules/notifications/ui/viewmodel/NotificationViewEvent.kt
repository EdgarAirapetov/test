package com.numplates.nomera3.modules.notifications.ui.viewmodel

sealed class NotificationViewEvent {

    object CloseFragment : NotificationViewEvent()

    object HideRefreshBtn : NotificationViewEvent()

    object ShowRefreshBtn : NotificationViewEvent()

    class OpenSupportAdminChat(val adminId: Long) : NotificationViewEvent()

    class SetLoadIndicatorVisibility(val isVisible: Boolean): NotificationViewEvent()

    class UpdateGlobalNotificationCounter(val count: Int): NotificationViewEvent()
}
