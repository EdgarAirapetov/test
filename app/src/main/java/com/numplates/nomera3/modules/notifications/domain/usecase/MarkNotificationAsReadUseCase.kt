package com.numplates.nomera3.modules.notifications.domain.usecase

import com.numplates.nomera3.modules.notifications.data.repository.NotificationRepository
import com.numplates.nomera3.modules.notifications.ui.entity.NotificationUiModel
import javax.inject.Inject

class MarkNotificationAsReadUseCase @Inject constructor(
    private val notificationRepo: NotificationRepository
) {
    suspend fun invoke(notificationId: String, isGroup: Boolean, unreadNotifications: List<NotificationUiModel>) {
        notificationRepo.markNotificationAsRead(
            notificationId = notificationId,
            isGroup = isGroup,
            unreadNotifications = unreadNotifications
        )
    }
}
