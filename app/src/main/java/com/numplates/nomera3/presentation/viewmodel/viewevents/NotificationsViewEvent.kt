package com.numplates.nomera3.presentation.viewmodel.viewevents

sealed class NotificationsViewEvent {

    object OnGetEvents : NotificationsViewEvent()

    object OnMarkNotificationsAsRead : NotificationsViewEvent()

    class OnSuccessNotificationCounter(val counter: Int): NotificationsViewEvent()


}