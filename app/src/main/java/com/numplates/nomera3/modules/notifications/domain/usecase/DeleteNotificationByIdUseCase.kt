package com.numplates.nomera3.modules.notifications.domain.usecase

import com.numplates.nomera3.modules.notifications.data.repository.NotificationRepository
import javax.inject.Inject

class DeleteNotificationByIdUseCase @Inject constructor(
    private val nRepository: NotificationRepository
) {

    suspend operator fun invoke(notificationId: String, isGroup: Boolean): Boolean =
        nRepository.deleteNotificationSuspend(notificationId = notificationId, isGroup = isGroup)
}
